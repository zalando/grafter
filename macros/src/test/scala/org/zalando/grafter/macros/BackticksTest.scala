package org.zalando.grafter.macros

object BackticksTest {

  type `X-tension` = String

  @reader
  case class Extension() {
    def extend(x: `X-tension`): `X-tension` = x
  }


}
