package org.zalando.grafter.macros

import cats.data.Reader

object ReadersMacroTest {
  val r1: Reader[AppConfig, C1] =
    AppConfig.c1Reader
  
  val r1Implicit: Reader[AppConfig, C1] =
    implicitly[Reader[AppConfig, C1]]

  val r2: Reader[AppConfig, C2] =
    AppConfig.c21Reader

  val r2Implicit: Reader[AppConfig, C2] =
    implicitly[Reader[AppConfig, C2]]

  val r: Reader[AppConfig, AppConfig] =
   AppConfig.appConfigReader

  val rImplicit: Reader[AppConfig, AppConfig] =
    implicitly[Reader[AppConfig, AppConfig]]


// this doesn't compile because only one Reader instance of a given type can be generated
//  val r3: Reader[AppConfig, C2] =
//    AppConfig.c22Reader
}
trait T1
trait T2

@readers
case class AppConfig(c1: C1, c21: C2, c22: C2)

object AppConfig extends T1 with T2 {
  def prod = AppConfig(C1(), C2(), C2())
}

case class C1()
case class C2()
