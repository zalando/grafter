package org.zalando.grafter

import org.zalando.grafter.macros.reader

object TestApplication extends UserGuidePage { def is = "Test the application".title ^ s2"""

For integration testing you generally need to replace components which are at the frontier of your system and
deeply embedded in your application.

This is can be done with the `replace` method:${snippet{
// 8<--
trait ApplicationConfig
object ApplicationConfig extends ApplicationConfig {
  def prod: ApplicationConfig = this
}
case class HttpConfig(host: String, port: Int)

@reader
case class Application()

// 8<--
import org.zalando.grafter.syntax.rewriter._

trait Database
object mockDatabase extends Database {
  // mock the database operations
}

// you can also replace the production configuration!
val testConfiguration =
  ApplicationConfig.prod.
    replace[HttpConfig](HttpConfig("localhost", 8080))

// create the application from the test configuration
// and mock the database
val application: Application =
  Application.reader.apply(testConfiguration).
    singletons.
    replace[Database](mockDatabase)

application.startAll
}}

*Note*: due to a limitation with the rewriting, final case classes with one arguments *cannot* be replaced!

It is also particularly important to test that the ${"application itself is properly configured" ~/ TestConfiguration}.

"""
}
