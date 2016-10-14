package org.zalando.grafter

import shapeless.{::, HNil, HList, Witness, LabelledGeneric, Lazy}
import shapeless.labelled.{FieldType, field}

import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an implicit FromConfig[${A}, ${B}]. Please make sure all members of ${B} have an instance of FromConfig[${A}, ...] and the implicit declarations have explicit type annotations.")
trait FromConfig[A, B] {

  def apply(a: A): B
}

object FromConfig {

  def apply[A, B](a: A)(implicit fc: FromConfig[A, B]): B = fc.apply(a)

  def const[A, B](b: B): FromConfig[A, B] = new FromConfig[A, B] {
    def apply(a: A): B = b
  }

  def embed[A, B](f: A => B): FromConfig[A, B] = new FromConfig[A, B] {
    def apply(a: A):B = f(a)
  }

  implicit def hnilFromConfigA[A]: FromConfig[A, HNil] = new FromConfig[A, HNil] {
    def apply(a: A): HNil =  HNil
  }

  implicit def aFromConfigHCons[A, K <: Symbol, H, T <: HList](implicit
    key: Witness.Aux[K],
    hFromConfigA: Lazy[FromConfig[A, H]],
    tFromConfigA: Lazy[FromConfig[A, T]]
  ): FromConfig[A, FieldType[K, H] :: T] = new FromConfig[A, FieldType[K, H] :: T] {

    def apply(a: A): FieldType[K,H] :: T = {
      val h = hFromConfigA.value(a)
      val t = tFromConfigA.value(a)
      field[K](h) :: t
    }
  }

  implicit def genericFromConfig[A, B, ReprB](implicit
    gen: LabelledGeneric.Aux[B, ReprB],
    reprbFromConfigA: Lazy[FromConfig[A, ReprB]]
  ): FromConfig[A, B] = new FromConfig[A, B] {

    def apply(a: A): B = {
      val internal = reprbFromConfigA.value(a)
      gen.from(internal)
    }
  }
}
