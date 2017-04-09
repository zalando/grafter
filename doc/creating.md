
### Creating other components and using shapeless

If a component depends on other components, its `Reader` instance depends on its dependencies
`Reader` instances. Since this is all recursive and automatable, thanks to Shapeless, we can
write the following:

```scala
import org.zalando.grafter.GenericReader._

case class Application(httpServer: HttpServer, db: Database)

object Application {

  // shapeless will automatically find Reader instances for HttpServer and Database
  implicit def reader: Reader[ApplicationConfig, Application] =
    genericReader
}

trait Database

object Database {
  implicit def reader: Reader[ApplicationConfig, Database] =
    PostgresDatabase.reader
}

case class PostgresDatabase(dbConfig: DbConfig) extends Start with Database {
  def start: Eval[StartResult] =
    StartResult.eval("postgres") {
      // use dbConfig.url to initialize
    }
}

object PostgresDatabase {
  implicit def reader: Reader[ApplicationConfig, PostgresDatabase] =
    genericReader
}

object DbConfig {
  implicit def reader: Reader[ApplicationConfig, DbConfig] =
    Reader(_.db)
}
```

Note that `Application` depends on the `Database` interface. When we create an `Application`
instance, `Database.reader` will be used and will provide a `Postgres` implementation by default.
This means that there must *always* be a default implementation for each interface introduced
in the system. But don't worry, we can always change it later!


#### Remove dependency on global config

You may be wondering why a `HttpServer` statically depends on the `ApplicationConfig` here:

```scala
object HttpServer {
  implicit def reader: Reader[ApplicationConfig, HttpServer] =
    genericReader
}
```

If you are creating a library, you will probably want to avoid this. To do it, lets parametrize
the `reader` function with some config of type `A`:

```scala
object HttpServer {
  implicit def dependentReader[A](implicit 
    httpConfigReader: Reader[A, HttpConfig]
  ): Reader[A, HttpServer] = genericReader
}
```

This allows us to put the `HttpServer` into a reusable module and build it independently
from the `ApplicationConfig`. Next, implicitly provide a `Reader[ApplicationConfing, HttpConfig]`
and you may create the `HttpServer`.


#### Create the full application

First you need a full `ApplicationConfig`

```scala
val prod: ApplicationConfig = ApplicationConfig(
  http = HttpConfig("localhost", 8080)
  db   = DbConfig("jdbc:localhost/database")
)
```

Then we can summon the implicit `Reader` instance for `Application` and pass it the "prod"
configuration:

```scala
val application: Application =
  GenericReader[ApplicationConfig, Application].run(prod)
```
