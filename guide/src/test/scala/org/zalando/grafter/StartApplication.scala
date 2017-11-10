package org.zalando.grafter

object StartApplication extends UserGuidePage { def is = "Start the application".title ^ s2"""

An application which is ${"instantiated" ~/ Concepts} and has ${"proper singletons" ~/ CreateSingletons} can now be started.

The components which can be "started" need to implement the `Start` interface and define a `start` method: ${snippet{
// 8<--
trait DatabaseConfig
// 8<--
import org.zalando.grafter.{Start, StartResult}
import cats.Eval

case class DoobieDatabase(config: DatabaseConfig) extends Start {
  def start: Eval[StartResult] =
    StartResult.eval("starting the database") {
      println("start the database here")
    }
}

}}

Then starting the whole application is just a matter of calling `startAll`:${snippet{
// 8<--
trait DatabaseConfig
import org.zalando.grafter._
import cats.Eval
val application = List[Int]()
// 8<--
import org.zalando.grafter.syntax.rewriter._

val start: Eval[List[StartResult]] =
  application.startAll

val results = start.value

if (results.forall(_.success))
  println("ok")
else
  println("Something went wrong "+results.mkString("\n"))
}}

`startAll` is going to recursively, **bottom up**, call all the components with a `Start` interface and collect the
`StartResults`.

### Stop the application

The application can also be stopped in the same manner. `stopAll` will stop each component implementing `Stop` from the top down.

The major difference between the `startAll` and `stopAll` is that *all* the components will try to be stopped regardless of failures.

"""
}
