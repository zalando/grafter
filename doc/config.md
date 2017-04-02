
### Configuration

The configuration of the application is a `case class`, possibly containing other case classes.
For the examples below we will use the following:

```scala
case class ApplicationConfig(
  http: HttpConfig,
  db:   DbConfig
)

case class HttpConfig(host: String,
                      port: Int)

case class DbConfig(url: String)
```

#### Create the first component

A component of the application is a case class. It must know how to instantiate itself from the
configuration. In other words, there needs to be a [`Reader[ApplicationConfig, Component]`](http://eed3si9n.com/herding-cats/Reader.html)
instance in scope.

For example
```scala
case class HttpServer(config: HttpConfig)

import cats.data.Reader
import cats.implicits._

object HttpServer {
  // we can "map" on a Reader!
  implicit def reader: Reader[ApplicationConfig, HttpServer] =
    HttpConfig.reader.map(HttpServer.apply)

}

object HttpConfig {
  // the HttpConfig is extracted directly from the application config
  implicit def reader: Reader[ApplicationConfig, HttpConfig] =
    Reader(_.http)
}
```

#### Remove some boilerplate

Ultimately configuration components like `DbConfig` above are extracted from `ApplicationConfig`.

It is possible to generate `Reader` instances for those values automatically with the `@readers` annotation:

```scala
import org.zalando.grafter.macros._

@readers
case class ApplicationConfig(
  db:   DbConfig,
  http: HttpConfig
)
```

Another common element is the `implicit def reader` that returns `genericReader`. It is possible to replace the
companion object containing this function with a `@reader[A]` annotation on the case class:

```scala
@reader[ApplicationConfig]
case class PostgresDatabase(dbConfig: DbConfig) extends Start {
  def start: Eval[StartResult] =
    Start.eval("postgres")(PostgresDriver.start(dbConfig.url))
}

// with the annotation you don't need the companion object
```

To create dependent reader use `@dependentReader` annotation:

```scala
@dependentReader
case class PostgresDatabase(dbConfig: DbConfig)
```

#### Integrate Grafter to your application

In order for the `GenericReader` functionality to work you need to
import it where you want `Reader` instances to be generated.

You can:
 
  - create a package object for your project to contain a few convenience aliases for your specific configuration type
  
  - have your application configuration object (say `ApplicationConfig`) extend the `GenericReader` trait to import the generic implicits into
  its implicit scope so that they are found whenever a component `X` tries to build a `Reader[ApplicationConfig, X]`
  
  - use the `@readers` annotation to your `ApplicationConfig` to generate the necessary readers for finding sub-configuration values,
  for example `Reader[ApplicationConfig, HttpConfig]`
  
```scala
package com.acme

import org.zalando.grafter
import shapeless._

package object config {

  type ConfigReader[A] = Reader[ApplicationConfig, A]

  def createReader[A, B](implicit gen: LabelledGeneric.Aux[A, B], repr: Lazy[ConfigReader[B]]): ConfigReader[A] =
    genericReader[ApplicationConfig, A, B](gen, repr)

  def configure[A](c: ApplicationConfig)(implicit r: ConfigReader[A]): A =
    r(c)

}

@readers
object ApplicationConfig extends GenericReader
```
