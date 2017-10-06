package org.zalando.grafter.examples.simple

import org.zalando.grafter.syntax.rewriter._

object Application {
  def main(args: Array[String]): Unit = {
    val config = ApplicationConfig.test
    val components = ApplicationComponents.reader[ApplicationConfig].apply(config).configure(config)

    println(components.startAll)
  }
}
