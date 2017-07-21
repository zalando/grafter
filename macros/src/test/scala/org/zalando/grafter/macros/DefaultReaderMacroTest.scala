package org.zalando.grafter.macros

import cats.data._

object DefaultReaderMacroTest {
  @defaultReader[E1]
  trait E

  case class E1() extends E

  case class E2() extends E

  case class Config()

  implicit val e1: Reader[Config, E1] = Reader(_ => E1())

  val r1: Reader[Config, E] = E.reader[Config]
}

