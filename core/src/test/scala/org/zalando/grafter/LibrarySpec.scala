package org.zalando.grafter

import cats.data.Reader
import org.specs2.Specification

class LibrarySpec extends Specification { def is = s2"""

  in a library $library

"""

  def library = {
    implicitly[Reader[AppConfig, Application]].apply(AppConfig.test).server.config.host ==== "host"
  }

  case class Application(server: HttpServer)

  object Application {
    implicit def reader: Reader[AppConfig, Application] =
      implicitly[Reader[AppConfig, HttpServer]].map(Application.apply)
  }

  case class AppConfig(httpServerConfig: HttpServerConfig)

  object AppConfig {

    implicit def httpServerConfigReader: Reader[AppConfig, HttpServerConfig] =
      Reader(_.httpServerConfig)

    val test: AppConfig = AppConfig(
      HttpServerConfig("host", 8080)
    )

    implicit def compose[A, B](implicit t: Reader[B, A], s: Reader[AppConfig, B]): Reader[AppConfig, A] =
      GenericReader.composeReaders(t, s)

  }

  case class HttpServerConfig(host: String, port: Int)

  case class HttpServer(config: HttpServerConfig)

  object HttpServer {
    implicit def reader: Reader[HttpServerConfig, HttpServer] =
      Reader(HttpServer.apply)
  }


}
