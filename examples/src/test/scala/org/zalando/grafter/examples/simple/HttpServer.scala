package org.zalando.grafter.examples.simple

import cats.Eval
import org.zalando.grafter.{Start, StartResult}
import org.zalando.grafter.macros.reader

@reader
case class HttpServer(config:          HttpServerConfig,
                      executorService: ExecutorService) extends Start {
  def start: Eval[StartResult] =
    StartResult.eval("http-server")(println("starting the http server"))
}

case class HttpServerConfig(host: String, port: Int)
