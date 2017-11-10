package org.zalando.grafter

import cats.data.Reader

object Concepts extends UserGuidePage { def is = "Main concepts".title ^ s2"""

### Readers all the way down

An application such as the one shown in ${"QuickStart" ~/ QuickStart} is merely a case class having its dependencies
modeled as attributes.

Similarly the application configuration is a case class containing every piece of information needed to build the `Application`.
How do we connect the 2?

This is the purpose of the `@reader` annotation on `Application`. The `@reader` annotation
generates a `reader` method in the companion object of `Application`: ${snippet{
// 8<--
trait HttpServer
trait Database
case class Application(httpServer: HttpServer, db: Database)
// 8<--
import cats.data.Reader

object Application {

  implicit def reader[A](implicit r1: Reader[A, HttpServer], r2: Reader[A, Database]): Reader[A, Application] =
    Reader(a => Application(r1(a), r2(a)))

}

}}

The declaration above states that:

 - if there is a implicit `Reader` instance to create an `HttpServer` from any object of type `A`
 - if there is a implicit `Reader` instance to create a `Database` from any object of type `A`
 - *then* there is an implicit `Reader` instance to create an `Application` from any object of type `A`
<p/>

This means that we can instantiate the `Application` from an `ApplicationConfig`, provided that we get a way to instantiate
an `HttpServer` and a `Database` from an `ApplicationConfig`. The `@reader` annotation on `HttpServer` gives us: ${snippet{
// 8<--
case class HttpServer(config: HttpConfig)
trait HttpConfig
// 8<--
import cats.data.Reader

object HttpServer {

  implicit def reader[A](implicit r1: Reader[A, HttpConfig]): Reader[A, HttpServer] =
    Reader(a => HttpServer(r1(a)))

}
}}

How can we build an `HttpConfig` from the `ApplicationConfig`? This is the purpose of the
`@readers` annotation on `ApplicationConfig`. This will generate concrete readers for each member of `ApplicationConfig`: ${snippet{
// 8<--
trait HttpConfig
trait DbConfig
case class ApplicationConfig(httpConfig: HttpConfig, dbConfig: DbConfig)
import cats.data.Reader

// 8<--
object ApplicationConfig {

  implicit def httpConfigReader: Reader[ApplicationConfig, HttpConfig] =
    Reader(_.httpConfig)

  implicit def dbConfigReader: Reader[ApplicationConfig, DbConfig] =
    Reader(_.dbConfig)
}

}}

From there the magic of implicit resolution will give us a valid `Application.reader[ApplicationConfig]`. Well, almost.
You might have noticed that `Database` is an interface, not a case class. How can we instantiate such an interface from
the application configuration?

### Deal with interfaces

For interfaces we need a special annotation which will specify a concrete implementation to instantiate, the `@defaultReader`
annotation. It generates the following reader: ${snippet{
// 8<--
trait Database
trait PostgresDatabase extends Database
object PostgresDatabase { def reader[A]: Reader[A, PostgresDatabase] = ??? }
// 8<--

object Database {
  implicit def reader[A]: Reader[A, PostgresDatabase] =
    PostgresDatabase.reader[A]
}
}}

This `Reader` instance is simply delegating to the `Reader` instance of `PostgresDatabase` and we now know that we have
such an instance because there is a `@reader` annotation on `PostgresDatabase`.

### Warning

![warning](images/icon_failure_sml.gif) ** All the components must be totally side-effects free when instantiated! **
They must not start a database connection or a http server or even do some logging!

Indeed, when using `Readers` to create components, the same `Database` component can be instantiated from different paths
in the application graph and become "duplicated" at that stage (see ${"create singletons" ~/ CreateSingletons} to fix this).
So it is particularly important that the "start" of an application is done in a very controlled way: ${"start the application" ~/ StartApplication}.

### Summary

In summary, to wire an application with Grafter you need to annotate:

 - components with `@reader`
 - interfaces with `@defaultReader`
 - the configuration with `@readers`
<p/>

There are a few more things you might need to know:

 - how to ${"create singletons" ~/ CreateSingletons}?
 - how to ${"start the application" ~/ StartApplication}?
 - how to ${"test the application" ~/ TestApplication}?

"""
}
