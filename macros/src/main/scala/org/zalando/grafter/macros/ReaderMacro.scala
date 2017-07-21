package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.meta._
import ReaderMacros._

/**
 * This macro extracts all the fields of a component and builds a Reader[A, ComponentType] instance
 * requiring an implicit reader instance for each field type
 */
object ReaderMacro {

  val annotation = "reader"

  def expand(classDef: Defn.Class, objectDef: Option[Defn.Object]): Term.Block = {
    classDef.ctor.paramss.toList match {
      case Nil =>
        output(classDef, objectDef)()

      case params :: _ =>
        val parameters = collectParamTypesAndNamesAsList(params)

        val implicitParameters = parameters.map { case (fieldType, fieldName) =>
          param"""${Term.Name(fieldName.value+"Reader")}: cats.data.Reader[A, $fieldType]"""
        }.map { p => p.copy(p.mods :+ Mod.Implicit()) }

        val paramNames = parameters.map(_._2)
        val readValues = paramNames.map { p => q"""val ${Pat.Var.Term(Term.Name("_"+p.value+"Value"))} = ${Term.Name(p.value+"Reader")}.apply(r);""" }
        val values     = paramNames.map { p => q"""${Term.Name("_"+p.value+"Value")}""" }

        output(classDef, objectDef) {
          q"""
            implicit def reader[A](..$implicitParameters): cats.data.Reader[A, ${classDef.name}] = {
              cats.data.Reader { r =>
                ..$readValues
                ${Term.Name(classDef.name.value)}(...${List(values)})
              }
            }
          """
        }
    }
  }

}

class reader extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    val (classDef, objectDef) = annotatedClass("reader")(defn)
    ReaderMacro.expand(classDef, objectDef)
  }

}
