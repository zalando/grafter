package org.zalando.grafter.examples.simple

import java.util.concurrent.Executors

import cats.Eval
import org.zalando.grafter._
import org.zalando.grafter.macros.reader

@reader
case class ExecutorService(config: ThreadPoolConfig) extends Start with Stop {
  implicit lazy val executor = Executors.newFixedThreadPool(config.threadsNb)

  def start: Eval[StartResult] =
    StartResult.eval("executor") {
      executor
    }

  def stop: Eval[StopResult] =
    StopResult.eval("executor") {
      executor.shutdown
    }
}

case class ThreadPoolConfig(threadsNb: Int)
