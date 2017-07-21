package org.zalando.grafter.macros

case class D1()

case class D2()

@reader
case class D(d1: D1, d2: D2)

object ReaderMacroTest {
  case class Config()

  //Absence one of these readers should raise compilation error
  implicit val rc1: cats.data.Reader[Config, D1] = cats.data.Reader(_ => D1())
  implicit val rc2: cats.data.Reader[Config, D2] = cats.data.Reader(_ => D2())

  val r1: cats.data.Reader[Config, D] = D.reader[Config]
}

