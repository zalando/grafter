package org.zalando.grafter.macros

case class D1()

case class D2()

@dependentReader
case class D(d1: D1, d2: D2)

object DependentReaderMacroTest {
  case class Config()

  //Absence one of these readers should raise compilation error
  implicit val rc1: cats.data.Reader[Config, D1] = null
  implicit val rc2: cats.data.Reader[Config, D2] = null
  val r1: cats.data.Reader[Config, D] = D.dependentReader[Config]
}

