package org.zalando.grafter

object QuickStart extends UserGuidePage { def is = "Quick Start".title ^ s2"""

### Installation

To use Grafter you need add it as a dependency in your sbt build settings:

```scala
libraryDependencies += "org.zalando" %% "grafter" % "$version"

// you also need the macros plugin for the grafter annotations
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

### Your first application

Here is a minimal example of an application using grafter ${snippet{

import org.zalando.grafter.macros.{readers, reader, defaultReader}
import org.zalando.grafter.{Start, StartResult}
import org.zalando.grafter.syntax.rewriter._
import cats.Eval

// CONFIGURATION
@readers
case class ApplicationConfig(
 http: HttpConfig,
 db:   DbConfig)

case class HttpConfig(host: String, port: Int)

case class DbConfig(url: String)

// COMPONENTS
@reader
case class HttpServer(config: HttpConfig) extends Start {
  def start: Eval[StartResult] =
    StartResult.eval("starting the http server")(println("http server started"))
}

@defaultReader[PostgresDatabase]
trait Database {
  def count(query: String): Int
}

@reader
case class PostgresDatabase(config: DbConfig) extends Database with Start {
  def count(query: String): Int = 0

  def start: Eval[StartResult] =
    StartResult.eval("starting the database")(println("db started"))
}

// TOP-LEVEL APPLICATION
@reader
case class Application(httpServer: HttpServer, database: Database)

// MAIN METHOD
def main(args: Array[String]): Unit = {

  val config: ApplicationConfig =
    ApplicationConfig(HttpConfig("localhost", 8080), DbConfig("jdbc://postgres"))

  val application: Application =
    Application.reader[ApplicationConfig].apply(config)

  val started = application.startAll.value

  if (started.forall(_.success))
    println("application started successfully")
  else
    println(started.mkString("\n"))
}
}}

As you can see in the main method, building the application from its configuration and starting it is just 2 lines of code.
Read the page on ${"concepts" ~/ Concepts} to understand how it all works.

"""
}
