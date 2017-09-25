package org.zalando.grafter.macros

// a reader annotation should not fail if there are no parameter lists
@reader
class D0

// a reader annotation should not fail if there are no parameters in the first parameter list
@reader
case class D1()

case class D2()

@reader
case class D(d1: D1, d2: D2)

object D {
  val somethingElse = 1
}

object ReaderMacroTest {
  case class Config()

  val rc0: cats.data.Reader[Config, D0] = implicitly[cats.data.Reader[Config, D0]]
  val rc1: cats.data.Reader[Config, D1] = implicitly[cats.data.Reader[Config, D1]]

  // Absence this reader should raise compilation error when trying to build r1
  implicit val rc2: cats.data.Reader[Config, D2] = cats.data.Reader(_ => D2())

  val r1: cats.data.Reader[Config, D] = D.reader[Config]
}

