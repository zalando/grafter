package org.zalando.grafter

import org.specs2.Specification
import cats.data._
import cats.implicits._
import GenericReader._

class GenericReaderSpec extends Specification { def is = s2"""

  An application can be "read" from a configuration by using
   Reader instances which are derived generically             $createApplication

"""

  def createApplication = {

    // create the configuration
    val allowed    = List("lookup price", "lookup stock")
    val httpConf   = HttpServerConfig("0.0.0.0", 80)
    val dbUri      = "localhost/somedb"

    val auth       = Authorization(allowed)
    val server     = HttpServer(httpConf.host, httpConf.port, auth)
    val db         = Database(dbUri)

    val appConfig  = AppConfig(allowed, httpConf, dbUri)

    // create the application
    val app        = configure[MicroService](appConfig)

    val haveServer = app.server === server
    val haveDb     = app.db     === db

    haveServer and haveDb
  }

  // Helpers

  case class AppConfig(allowed: List[String], httpConf: HttpServerConfig, dbUri: String)

  case class HttpServerConfig(host: String, port: Int)

  object HttpServerConfig {
    implicit def reader: Reader[AppConfig, HttpServerConfig] =
      Reader(conf => HttpServerConfig(conf.httpConf.host, conf.httpConf.port))
  }

  case class Authorization(allowed: List[String])

  type AppConfigReader[A] = Reader[AppConfig, A]

  def configure[A](c: AppConfig)(implicit r: Reader[AppConfig, A]): A =
    GenericReader[AppConfig, A].run(c)

  object Authorization {
    implicit def reader: AppConfigReader[Authorization] =
      Reader(conf => Authorization(conf.allowed))
  }

  case class HttpServer(host: String, port: Int, auth: Authorization)

  object HttpServer {
    implicit def reader: AppConfigReader[HttpServer] =
      Reader(conf => HttpServer(conf.httpConf.host, conf.httpConf.port, configure[Authorization](conf)))
  }

  case class Database(dbUri: String)

  object Database {
    implicit def reader: AppConfigReader[Database] =
      Reader(conf => Database(conf.dbUri))
  }

  case class MicroService(server: HttpServer, db: Database)

  object MicroService {
    implicit def reader: Reader[AppConfig, MicroService] =
      genericReader
  }
}
