### Full application

[Creating components](creating.md) introduces the idea of using `Readers`
for dependency injection and progressively introduces annotations,
interfaces, start / stop methods, and rewriting.

Here we collect a fully executable
example which you can use as a template for your own applications:

`Application.scala`

```tut:silent:fail
import org.zalando.grafter.syntax.rewriter._

object Application {
  def main(args: Array[String]): Unit = {
    val config = ApplicationConfig.test
    val components =
      ApplicationComponents.reader[ApplicationConfig].apply(config).
      configure(config)

    println(components.startAll)
  }
}
```

The flow from the code above shows that:

 1. we create the application components from the configuration
 2. we further configure the application by making singletons and setting
    the right thread pools on individual components
 3. we start the application

`ApplicationComponents.scala`

```tut:fail
import org.zalando.grafter.macros._
import org.zalando.grafter.syntax.rewriter._

@reader
case class ApplicationComponents(
  server:   HttpServer,
  database: Database) {

  def configure(config: ApplicationConfig): ApplicationComponents =
    this.singletons.modify[Any] {
      case c: HttpServer       => c.replaceFirst(config.serverThreadPoolConfig)
      case c: PostgresDatabase => c.replaceFirst(config.databaseThreadPoolConfig)
    }

}
```

The `singletons` and `modify` methods are both provided by the implicits in
`org.zalando.grafter.syntax.rewriter._`.

`ApplicationConfig.scala`

```tut:fail
import org.zalando.grafter.GenericReader
import org.zalando.grafter.macros.readers

@readers
case class ApplicationConfig(
  httpServerConfig: HttpServerConfig,
  serverThreadPoolConfig: ThreadPoolConfig,
  databaseThreadPoolConfig: ThreadPoolConfig)

object ApplicationConfig extends GenericReader {

  def test: ApplicationConfig =
    ApplicationConfig(
      httpServerConfig         = HttpServerConfig("localhost", 8080),
      serverThreadPoolConfig   = ThreadPoolConfig(8),
      databaseThreadPoolConfig = ThreadPoolConfig(4))

}
```

`HttpServer.scala`

```tut:nofail
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
```

`Database.scala`

```tut:fail
import cats.Eval
import org.zalando.grafter.macros.defaultReader

@defaultReader[PostgresDatabase]
trait Database {

  def runQuery(query: String): Eval[Unit]

}
```

Notice that we indicate a default implementation for the `Database` trait
with the `@defaultReader` annotation.

`PostgresDatabase.scala`

```tut:fail
import cats.Eval
import org.zalando.grafter._
import org.zalando.grafter.macros.reader

@reader
case class PostgresDatabase(executorService: ExecutorService) extends Database with Start {

  def start: Eval[StartResult] =
    StartResult.eval("database") {
      println("migrate data")
    }

  def runQuery(query: String): Eval[Unit] =
    Eval.later(println("running query "+query))

}
```

----
Previous: [Components](components.md)

Next: [Your first component](creating.md)
