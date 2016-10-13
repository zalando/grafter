package org.zalando.conf4s

import shapeless.{::, HNil, HList, Witness, LabelledGeneric}
import shapeless.labelled.{FieldType, field}

trait FromConfig[A, B] {

  def apply(a: A): B
}

object FromConfig {

  def create[A, B](a: A)(implicit fc: FromConfig[A, B]): B = fc.apply(a)

  implicit def hnilFromConfigA[A]: FromConfig[A, HNil] = new FromConfig[A, HNil] {
    def apply(a: A): HNil =  HNil
  }

  implicit def aFromConfigHCons[A, K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    hFromConfigA: FromConfig[A, H],
    tFromConfigA: FromConfig[A, T]
  ): FromConfig[A, FieldType[K, H] :: T] = new FromConfig[A, FieldType[K, H] :: T] {

    def apply(a: A): FieldType[K,H] :: T = {
      val h = hFromConfigA(a)
      val t = tFromConfigA(a)
      field[K](h) :: t
    }
  }

  implicit def genericFromConfig[A, B, ReprB](implicit
    gen: LabelledGeneric.Aux[B, ReprB],
    reprbFromConfigA: FromConfig[A, ReprB]
  ): FromConfig[A, B] = new FromConfig[A, B] {

    def apply(a: A): B = {
      val internal = reprbFromConfigA(a)
      gen.from(internal)
    }
  }
}
