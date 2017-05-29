### Remove boilerplate with shapeless

Using `Reader` instances to define dependencies between components seems a bit over-engineered at first. But it is not, 
Reader being a "Type class" we can use Scala implicit resolution and [shapeless](https://github.com/milessabin/shapeless)
to define those instances for us:
```scala
import org.zalando.grafter.GenericReader._
import cats.data.Reader
import cats.implicits._

object ApplicationConfig {
  implicit def readerHttpConfig: Reader[ApplicationConfig, HttpConfig] =
    genericReader

  implicit def readerDbConfig: Reader[ApplicationConfig, DbConfig] =
    genericReader
}

object HttpServer {
  implicit def reader: Reader[ApplicationConfig, HttpServer] =
    genericReader
}

object PostgresDatabase {
  implicit def reader: Reader[ApplicationConfig, PostgresDatabase] =
    genericReader
}

object Application {
  implicit def reader: Reader[ApplicationConfig, Application] =
    genericReader 
}
```

### Remove boilerplate with annotations

The code above still looks a bit repetitive. Can it be automated? Absolutely, by using a macro annotation:

```scala
import org.zalando.grafter.macros._

@readers
case class ApplicationConfig(
  db:   DbConfig,
  http: HttpConfig
)
```

The `@readers` annotation creates an implicit `Reader` instance for each member of the `ApplicationConfig` class. With 
this annotation the implicit resolution will find how to extract specific configuration data from the overall configuration.

Then, for non-configuration components, like `HttpServer` we can also generate the `reader` code with a `@reader` annotation:
```scala
@reader[ApplicationConfig]
case class PostgresDatabase(dbConfig: DbConfig)
```

There is still a slight problem to solve? How can we put some components in libraries if they explicitly depend on `ApplicationConfig`
which is an application-specific class? 

----
Previous: [Create components](creating.md)

Next: [In a library](library.md)
