package org.zalando.grafter.macros

object ReaderMacroTest {
  val r1: cats.data.Reader[AppConfig, C1] =
    AppConfig.c1Reader

  val r2: cats.data.Reader[AppConfig, C2] =
    AppConfig.c21Reader

// this doesn't compile because only one Reader instance of a given type can be generated
//  val r3: cats.data.Reader[AppConfig, C2] =
//    AppConfig.c22Reader
}

@readers
case class AppConfig(c1: C1, c21: C2, c22: C2)

object AppConfig {
  def prod = AppConfig(C1(), C2(), C2())
}

case class C1()
case class C2()
