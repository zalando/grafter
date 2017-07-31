
### Create components

The first component we create is the configuration of the application. 
It is a `case class`, possibly containing other case classes. For the examples below we will use the following:

```tut:silent:nofail
case class ApplicationConfig(
  http: HttpConfig,
  db:   DbConfig
)

case class HttpConfig(host: String,
                      port: Int)

case class DbConfig(url: String)
```

#### The next component

Our next component is a `HttpServer`. It needs its own piece of configuration, `HttpConfig`:
```tut:silent:nofail
case class HttpServer(config: HttpConfig)
``` 

How do we get an `HttpConfig` in the first place? 
We can get it from an `ApplicationConfig` using a [`Reader` instance](http://eed3si9n.com/herding-cats/Reader.html):

```tut:silent:fail
object HttpConfig {
  // the HttpConfig is extracted directly from the application config
  def reader: Reader[ApplicationConfig, HttpConfig] =
    Reader(_.http)
}
```

Then we can define an other `Reader` instance for `HttpServer` describing how to get a `HttpServer` from an `ApplicationConfig`:

```tut:silent:nofail
case class HttpServer(config: HttpConfig)

import cats.data.Reader
import cats.implicits._

object HttpServer {
  // we can "map" on a Reader!
  def reader: Reader[ApplicationConfig, HttpServer] =
    HttpConfig.reader.map(HttpServer.apply)

}
```

We now have:

 - a component describing the application configuration
 - a component for the specific configuration of the `HttpServer`
 - a component for the `HttpServer`
 - `reader` methods to create instances of those components

Let's scale this up to a full application.

#### Create the full application

The application is a top-level component, depending on the `HttpServer` and a new component, the `PostgresDatabase`, also having
its own configuration:

```tut:silent:nofail
case class PostgresDatabase(dbConfig: DbConfig)

object PostgresDatabase {
  def reader: Reader[ApplicationConfig, PostgresDatabase] =
    DbConfig.reader.map(PostgresDatabase.apply)
}

object DbConfig {
  def reader: Reader[ApplicationConfig, DbConfig] =
    Reader(_.db)
}
```

Then the full application:

```tut:silent:nofail
case class Application(httpServer: HttpServer, db: PostgresDatabase)

object Application {

  // Reader has a Monad instance so we can use it in a for comprehension
  implicit def reader: Reader[ApplicationConfig, Application] =
    for {
       server   <- HttpServer.reader
       database <- PostgresDatabase.reader
    } yield Application(server, database)
    
}
```

Finally we create an instance of `ApplicationConfig` (we can deserialize this object from a file if necessary):

```tut:silent:fail
val prod: ApplicationConfig = ApplicationConfig(
  http = HttpConfig("localhost", 8080),
  db   = DbConfig("jdbc:localhost/database")
)
```

And get our fully wired application:

```tut:silent:fail
val application: Application =
  Application.reader.apply(prod)
```

This simple example shows that we can structure an application as a set of case classes, and instantiate it with `Reader` methods.
Next we will see how to remove all the boilerplate above!

----
Previous: [Grafter components](components.md)

Next: [Remove boilerplate](boilerplate.md)
