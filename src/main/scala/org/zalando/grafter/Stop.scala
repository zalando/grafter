package org.zalando.grafter

import cats.Eval

trait Stop {
  def stop: Eval[StopResult]
}

sealed trait StopResult {
  def success: Boolean
}

case class StopOk(message: String)                          extends StopResult { def success = true }
case class StopFailure(message: String)                     extends StopResult { def success = false }
case class StopError(message: String, exception: Throwable) extends StopResult { def success = false }

object StopResult {
  def eval[A](message: String)(a: =>A): Eval[StopResult] =
    Eval.later {
      try { a; StopOk(message) }
      catch { case t: Throwable => StopError(message, t) }
    }
}
