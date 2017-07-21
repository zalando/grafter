package org.zalando.grafter.examples.simple

import org.zalando.grafter.GenericReader
import org.zalando.grafter.macros.readers

@readers
case class ApplicationConfig(
  httpServerConfig: HttpServerConfig,
  serverThreadPoolConfig: ThreadPoolConfig,
  databaseThreadPoolConfig: ThreadPoolConfig)

object ApplicationConfig extends GenericReader {

  def test: ApplicationConfig =
    ApplicationConfig(
      httpServerConfig         = HttpServerConfig("localhost", 8080),
      serverThreadPoolConfig   = ThreadPoolConfig(8),
      databaseThreadPoolConfig = ThreadPoolConfig(4))

}
