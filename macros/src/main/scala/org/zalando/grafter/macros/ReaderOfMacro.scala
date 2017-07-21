package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.meta._
import ReaderMacros._
import ReaderOfMacro._

/**
 * This macro extracts all the fields of a component and builds a Reader[A, ComponentType] instance
 * requiring an implicit reader instance for each field type where A is the type referred by the readerOf[A]
 * annotation
 */
object ReaderOfMacro {

  val annotationName = "readerOf"

  def expand(classDef: Defn.Class, objectDef: Option[Defn.Object], typeParam: Type.Name): Term.Block = {
    classDef.ctor.paramss.toList match {
      case Nil =>
        output(classDef, objectDef)()

      case params :: _ =>
        val parameters = collectParamTypesAndNamesAsList(params)

        val paramNames = parameters.map(_._2)
        val implicits  = parameters.map { case (_type, p) => q"""private val ${Pat.Var.Term(Term.Name("_"+p.value+"Reader"))} = implicitly[cats.data.Reader[$typeParam, ${_type}]];""" }
        val readValues = paramNames.map { p => q"""val ${Pat.Var.Term(Term.Name(p.value+"Value"))} = ${Term.Name("_"+p.value+"Reader")}.apply(r);""" }
        val values     = paramNames.map { p => q"""${Term.Name(p.value+"Value")}""" }

        val reader =
          q"""
            implicit def reader: cats.data.Reader[$typeParam, ${classDef.name}] = {
              cats.data.Reader { r =>
                ..$readValues
                ${Term.Name(classDef.name.value)}(...${List(values)})
              }
            }
          """

        output(classDef, objectDef)(implicits :+ reader:_*)

    }
  }

}

class readerOf[A] extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    val (classDef, objectDef) = annotatedClass(annotationName)(defn)
    ReaderOfMacro.expand(classDef, objectDef, Type.Name(A.toString))
  }

}
