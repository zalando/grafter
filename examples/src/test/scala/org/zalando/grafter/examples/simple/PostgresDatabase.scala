package org.zalando.grafter.examples.simple

import cats.Eval
import org.zalando.grafter._
import org.zalando.grafter.macros.reader

@reader
case class PostgresDatabase(executorService: ExecutorService) extends Database with Start {

  def start: Eval[StartResult] =
    StartResult.eval("database") {
      println("migrate data")
    }

  def runQuery(query: String): Eval[Unit] =
    Eval.later(println("running query "+query))

}
