package org.zalando.grafter.macros


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
case class HttpClient(config: HttpSubConfig2)


@reader
case class SomeService(client: HttpClient)

@reader
case class WebApplication(someService: SomeService)


object MyApp extends App {

  val config = ApplicationConfiguration(HttpConfig(HttpSubConfig1(80), HttpSubConfig2("bla")))

  val app = WebApplication.reader[ApplicationConfiguration].apply(config)

}

