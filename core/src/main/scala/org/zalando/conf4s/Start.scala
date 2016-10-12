package org.zalando.conf4s

import cats.Eval

trait Start {
  def start: Eval[StartResult]
}

sealed trait StartResult {
  def success: Boolean
}

case class StartOk(message: String)                          extends StartResult { def success = true }
case class StartFailure(message: String)                     extends StartResult { def success = false }
case class StartError(message: String, exception: Throwable) extends StartResult { def success = false }

object StartResult {
  def eval[A](message: String)(a: =>A): Eval[StartResult] =
    Eval.later {
      try { a; StartOk(message) }
      catch { case t: Throwable => StartError(message, t) }
    }
}
