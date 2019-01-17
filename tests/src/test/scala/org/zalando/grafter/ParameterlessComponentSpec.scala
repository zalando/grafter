package org.zalando.grafter.macros


/**
 *  This is a compilation test to check that a component without parameters
 *  can still be injected. see #124
 */
object ParameterlessComponentSpec {
  @readers
  case class ApplicationConfiguration(httpConfig: HttpConfig, additionalConfig: AdditionalConfig)

  /**
   * For a nested configuration you need to annotate the nested elements with the @readers annotation
   * as well
   */
  @readers
  case class HttpConfig(cfg1: HttpSubConfig1, cfg2: HttpSubConfig2)

  case class HttpSubConfig1(port: Int)
  case class HttpSubConfig2(host: String)
  case class AdditionalConfig(config3: String)

  @reader
  case class HttpClient(config: HttpSubConfig2)


  @reader
  case class SomeService(client: HttpClient)

  @reader
  case class SomeServiceWithoutParameters()

  @reader
  case class WebApplication(someService: SomeService, emptyService: SomeServiceWithoutParameters)


  object MyApp extends App {

    val config = ApplicationConfiguration(
        HttpConfig(HttpSubConfig1(80),  HttpSubConfig2("bla")),
        AdditionalConfig("3")
    )

    val app = WebApplication.reader[ApplicationConfiguration].apply(config)

  }
}

