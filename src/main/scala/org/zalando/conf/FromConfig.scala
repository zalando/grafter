package org.zalando.conf

import shapeless._

trait FromConfig[A, B] {

  def apply(a: A): B
}

object FromConfig {

  implicit def aFromConfigHNil[A]: FromConfig[A, HNil] = new FromConfig[A, HNil] {
    def apply(a: A): HNil =  HNil
  }

  implicit def aFromConfigHCons[A, V, T <: HList](implicit
    aFromConfigV: FromConfig[A, V],
    aFromConfigT: FromConfig[A, T]
  ): FromConfig[A, V :: T] = new FromConfig[A, V :: T] {

    def apply(a: A): V :: T = {
      val v = aFromConfigV(a)
      val t = aFromConfigT(a)
      v :: t
    }
  }

  implicit def genericFromConfig[A, B, InternalB](implicit
    gen: Generic.Aux[B, InternalB],
    aFromConfigInternalB: FromConfig[A, InternalB]
  ): FromConfig[A, B] = new FromConfig[A, B] {

    def apply(a: A): B = {
      val internal = aFromConfigInternalB(a)
      gen.from(internal)
    }
  }
}
