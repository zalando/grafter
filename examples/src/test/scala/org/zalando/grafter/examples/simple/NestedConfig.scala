package org.zalando.grafter.examples.simple

import cats.Eval
import org.zalando.grafter.{Start, StartResult}
import org.zalando.grafter.macros._
import org.zalando.grafter.syntax.rewriter._

@readers
case class ApplicationConfiguration(httpConfig: HttpConfig)

/**
 * For a nested configuration you need to annotate the nested elements with the @readers annotation
 * as well
 */
@readers
case class HttpConfig(cfg1: HttpSubConfig1, cfg2: HttpSubConfig2)

case class HttpSubConfig1(port: Int)
case class HttpSubConfig2(host: String)

@reader
case class HttpClient(config: HttpSubConfig2) extends Start {
  override def start: Eval[StartResult] = StartResult.eval("HttpClient")(println("Start HttpClient"))
}


@reader
case class SomeService(client: HttpClient) extends Start {
  override def start: Eval[StartResult] = StartResult.eval("SomeService")(println("Start SomeService"))
}

@reader
case class WebApplication(someService: SomeService)


object MyApp extends App {

  val config = ApplicationConfiguration(HttpConfig(HttpSubConfig1(80), HttpSubConfig2("bla")))

  val app = WebApplication.reader[ApplicationConfiguration].apply(config).singletons

  val started = app.startAll.value

  if (started.forall(_.success))
    println("application started successfully")
  else
    println(started.mkString("\n"))
}
