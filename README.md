# Grafter

![grafting](https://autonomyacres.files.wordpress.com/2015/04/crown-cleft-grafting-fruit-trees.jpg?w=300&h=284)

# What's wrong with constructor injection again?

There are [many](https://github.com/adamw/macwire) [libraries](https://github.com/google/guice) or [approaches](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth) for doing [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) in Scala.
Grafter goes back to the fundamentals of dependency injection by *just using constructor injection*: no reflection, no xml, no annotations, no inheritance or self-types.
 
Then, Grafter add to constructor injection just the necessary support to:

 - instantiate a component-based application from a configuration
 - fine-tune the wiring (create singletons)
 - test the application by replacing components
 - start / stop the application

Grafter is targeting every possible application because it focuses on associating just 3 ideas:

 - case classes and interfaces for components
 - `Reader` instances and [shapeless](http://github.com/milessabin/shapeless) for the configuration
 - [tree rewriting](http://www.program-transformation.org/Transform/TreeRewriting) and [kiama](https://bitbucket.org/inkytonik/kiama) for everything else!

Please try it and report your experience:

 - how is it better / worse than another library?
 - is the core model more approachable than other libraries?
 - what could be improved?

# Grafter components

Grafter components are very simple: they are just case classes (possibly) implementing interfaces.

 1. components can depend on other components by having them as case class members
 2. the application is the top-level component
 3. the application configuration is a just a case class (can be read from a file if necessary)
 4. each component can be instantiated from the application configuration
 5. so it is possible to recursively build the full application as a
 *tree* of components from the application configuration
 6. singletons can be made by rewriting the tree, effectively making it a graph
 7. the application can be started bottom-up by starting the components
 extending the `Start` markup trait
 8. the application can also be stopped top-down by stopping all the components
 extending the `Stop` markup trait
 8. mocking the application for testing can also be done by rewriting the
 tree

Let's see this on a concrete example.

### Create the application configuration

The configuration of the application is a case class possibly containing
 other case classes. For the examples below we will use

```scala
case class ApplicationConfig(
  http: HttpConfig,
  db:   DbConfig
)

case class HttpConfig(host: String,
                      port: Int)

case class DbConfig(url: String)
```

### Create the first component

A component of the application is a case class. It must know how to
 instantiate itself from the configuration. In other terms there needs to be
 a [`Reader[ApplicationConfig, Component]`](http://eed3si9n.com/herding-cats/Reader.html) instance in scope.

For example
```scala
case class HttpServer(config: HttpConfig)

import cats.data.Reader
import cats.implicits._

object HttpServer {
  // we can "map" on a Reader!
  implicit def reader: Reader[ApplicationConfig] =
    HttpConfig.reader.map(HttpServer.apply)

}

object HttpConfig {

  // the HttpConfig is extracted directly from
  // the application config
  implicit def reader: Reader[ApplicationConfig, HttpConfig] =
    Reader(_.http)

}
```

### Create other components and use shapeless

If a component depends on other components. Its `Reader` instance
  depends on its dependencies `Reader` instances. Since this is all recursive
  and automatable thanks to Shapeless we can write this

```scala

import org.zalando.grafter.GenericReader._

case class Application(httpServer: HttpServer, db: Database)

object Application {

  // shapeless will automatically find Reader instances for
  // HttpServer and Database
  implicit def reader: Reader[ApplicationConfig, Application] =
    genericReader

}

trait Database

object Database {
  implicit def reader: Reader[ApplicationConfig, Database] =
    PostgresDatabase.reader
}

case class PostgresDatabase(dbConfig: DbConfig) extends Start {
  def start: Eval[StartResult] =
    Start.eval("postgres")(PostgresDriver.start(dbConfig.url))
}

object PostgresDatabase {
  implicit def reader: Reader[ApplicationConfig, PostgresDatabase] =
    genericReader
}

object DbConfig {
  implicit def reader: Reader[ApplicationConfig, dbConfig] =
    Reader(_.db)
}

```

Note that `Application` depends on the `Database` interface. When we
will create an `Application` instance `Database.reader` will be used and
will provide a `Postgres` implementation by default. This means that
there must *always* be a default implementation for each interface introduced
in the system. But don't worry we can always change it later!

### Create the full application

First you need a full `ApplicationConfig`
```scala
val prod: ApplicationConfig = ApplicationConfig(
  http = HttpConfig("localhost", 8080)
  db   = DbConfig("jdbc:localhost/database")
)
```

Then we can summon the implicit `Reader` instance for `Application` and
pass it the "prod" configuration:
```scala
val application: Application =
  GenericReader[ApplicationConfig, Application].run(prod)
```

### Make singletons

The next step is making sure that however deep our application graph is,
we will always use one database, even if 2 components declare 2 dependencies
to the database. This is done with the `Rewriter` object:
```scala
import org.zalando.grafter.Rewriter

// can also be written application.singleton[Database]
// by importing Rewriter._
val app: Application =
  Rewriter.singleton[Database](application)
```

### Start the application

Now the application can be started, using the `Rewriter` again which is
going to traverse the application graph and start each component implementing
`Start` from the bottom up. If you scroll up you will see that `PostgresDatabase`
is such a component and must implement a `start` method returning a `StartResult`.

```scala
import cats._

val started: Eval[List[StartResult]] =
  Rewriter.start(app)
```

The `List[StartResult]` can be used to diagnose the start up and
 produce a nice error message if something went wrong.

### Stop the application

The application can also be stopped using the `Rewriter`. It will stop
each component implementing `Stop` from the top down.

```scala
import cats._

val stop: Eval[List[StopResult]] =
  Rewriter.stop(app)
```

The major difference between the start and the stop strategies is that
*all* the components will try to be stopped regardless of failures.

The `List[StopResult]` can be used to diagnose the shutdown and
 produce a nice error message if something went wrong.

### Test the application

For integration testing you generally need to replace components which are
at the frontier of your system and deeply embedded in your application.
This is done once again with `Rewriter`
```scala
import org.zalando.grafter._, Rewriter._

object mockDb extends Database {
  // mock the database operations
}

// you can also rewrite the prod configuration!
val testConfiguration =
  Application.prod.
    replace[HttpConfig](HttpConfig("localhost", 8080))

// create the application
// from the test config
// and mock the database
val application: Application =
  GenericReader[ApplicationConfig, Application].
    run(testConfiguration).
    singleton[Database].
    replace[Database](mockDb)
```

### Integrate Grafter to your application

In order for the `GenericReader` functionality to work you need to
import it where you want `Reader` instances to be generated.

You can also create a package object for your project, mix-in the
`GenericReader` trait and add a few convenience aliases for your project:

```scala
package com.acme

import org.zalando.grafter
import shapeless._

package object config extends GenericReader {

  type ConfigReader[A] = Reader[ApplicationConfig, A]

  def createReader[A, B](implicit gen: LabelledGeneric.Aux[A, B], repr: Lazy[ConfigReader[B]]): ConfigReader[A] =
    genericReader[ApplicationConfig, A, B](gen, repr)

  def configure[A](c: ApplicationConfig)(implicit r: ConfigReader[A]): A =
    r(c)

}
```

## Installation

You add this library as a sbt dependency:
```scala
libraryDependencies += "org.zalando" %% "grafter" % "1.0.0"
```

## Contributing

Please read our [contributor guidelines](CONTRIBUTING.md) for more details. 
And please check these [open issues](http://github.com/zalando/grafter/issues) for specific tasks.

----

## License

The MIT License (MIT) Copyright © [2016] Zalando SE, https://tech.zalando.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
