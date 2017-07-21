package org.zalando.grafter.examples.simple

import cats.Eval
import org.zalando.grafter.macros.defaultReader

@defaultReader[PostgresDatabase]
trait Database {

  def runQuery(query: String): Eval[Unit]

}
