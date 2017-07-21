package org.zalando.grafter.examples.simple

import org.zalando.grafter.macros._
import org.zalando.grafter.syntax.rewriter._

@reader
case class ApplicationComponents(
  server:   HttpServer,
  database: Database) {

  def configure(config: ApplicationConfig): ApplicationComponents =
    this.singletons.modify[Any] {
      case c: HttpServer       => c.replaceFirst(config.serverThreadPoolConfig)
      case c: PostgresDatabase => c.replaceFirst(config.databaseThreadPoolConfig)
    }

}
