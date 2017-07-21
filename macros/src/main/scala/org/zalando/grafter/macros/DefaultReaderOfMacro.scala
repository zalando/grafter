package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import ReaderMacros._

import scala.meta._
import DefaultReaderMacro._

object DefaultReaderMacroOf {

  val annotationName = "defaultReaderOf"

  def expand(traitDef: Defn.Trait, objectDef: Option[Defn.Object], configTypeParam: Type.Name, typeParam: Type.Name): Term.Block = {
    output(traitDef, objectDef) {
      q"""
         implicit def reader(implicit defaultReader: cats.data.Reader[$configTypeParam, $typeParam]): cats.data.Reader[$configTypeParam, ${traitDef.name}] =
           org.zalando.grafter.GenericReader.widenReader(defaultReader)
      """
    }
  }

}

class defaultReaderOf[A, B] extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    val (traitDef, objectDef) = annotatedTrait(annotationName)(defn)
    DefaultReaderMacroOf.expand(traitDef, objectDef, Type.Name(A.toString), Type.Name(B.toString))
  }

}
