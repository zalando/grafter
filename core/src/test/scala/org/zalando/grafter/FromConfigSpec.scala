package org.zalando.grafter

import org.specs2.Specification

class FromConfigSpec extends Specification { def is = s2"""
    FromConfig can be derived genericly $initApplication
"""

  def initApplication = {

    val allowed    = List("lookup price", "lookup stock")
    val httpConf   = HttpServerConfig("0.0.0.0", 80)
    val dbUri      = "localhost/somedb"

    val auth       = Authorization(allowed)
    val server     = HttpServer(httpConf.host, httpConf.port, auth)
    val db         = Database(dbUri)

    val appconfig  = AppConfig(allowed, httpConf, dbUri)
    val app        = FromConfig[AppConfig, ExampleApp](appconfig)

    val haveServer = app.server === server
    val haveDb     = app.db     === db

    haveServer and haveDb
  }

  // Helpers

  case class AppConfig(allowed: List[String], httpConf: HttpServerConfig, dbUri: String)
  case class HttpServerConfig(host: String, port: Int)

  case class Authorization(allowed: List[String])
  object Authorization {
    implicit def fromConfig: FromConfig[AppConfig, Authorization] = FromConfig.embed(conf => Authorization(conf.allowed))
  }

  case class HttpServer(host: String, port: Int, auth: Authorization)
  object HttpServer {
    implicit def fromConfig(implicit
      authFromConfig: FromConfig[AppConfig, Authorization]): FromConfig[AppConfig, HttpServer] =
      FromConfig.embed(conf => HttpServer(conf.httpConf.host, conf.httpConf.port, authFromConfig(conf)))
  }

  case class Database(dbUri: String)
  object Database {
    implicit def fromConfig: FromConfig[AppConfig, Database] = FromConfig.embed(conf => Database(conf.dbUri))
  }

  case class ExampleApp(server: HttpServer, db: Database)
}
