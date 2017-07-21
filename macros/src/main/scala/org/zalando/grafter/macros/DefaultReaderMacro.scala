package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import ReaderMacros._

import scala.meta._
import DefaultReaderMacro._

object DefaultReaderMacro {

  val annotationName = "defaultReader"

  def expand(traitDef: Defn.Trait, objectDef: Option[Defn.Object], typeParam: Type.Name): Term.Block = {
    output(traitDef, objectDef) {
      q"""
         implicit def reader[A](implicit defaultReader: cats.data.Reader[A, $typeParam]): cats.data.Reader[A, ${traitDef.name}] =
           org.zalando.grafter.GenericReader.widenReader(defaultReader)
      """
    }
  }

}

class defaultReader[A] extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    val (traitDef, objectDef) = annotatedTrait(annotationName)(defn)
    DefaultReaderMacro.expand(traitDef, objectDef, Type.Name(A.toString))
  }

}
