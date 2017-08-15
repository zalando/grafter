package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import ReaderMacros._

/**
 * This macro extracts all the fields of a component and builds a Reader[A, ComponentType] instance
 * requiring an implicit reader instance for each field type
 */
object ReaderMacro {

  val annotationName = "reader"

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    def name(t: Name) = t.decodedName.toString.trim

    val (classTree, companionTree): (Tree, Option[Tree]) =
      annotationInputs(c)(annotationName)(annottees)

    val genericTypeName = internal.reificationSupport.freshTypeName("A")

    classTree match {
      case ClassDef(_, className, _, Template(_, _, fields)) =>
        val params = fieldsNamesAndTypes(c)(fields)

        val implicitParameters = params
          .map { case (fieldName, fieldType) =>
            val readerName = TermName(name(fieldName)+"Reader")
            c.Expr[ValDef](q"""$readerName: cats.data.Reader[$genericTypeName, $fieldType]""")
          }

        val paramNames = params.map(_._1)
        val readValues = paramNames.map { p => q"""val ${TermName("_"+name(p)+"Value")} = ${TermName(name(p)+"Reader")}.apply(r);""" }
        val values     = paramNames.map { p => q"""${TermName("_"+name(p)+"Value")}""" }
        val klassName  = name(className)

        outputs(c)(classTree, className, companionTree) {
          q"""
         implicit def reader[$genericTypeName](implicit ..$implicitParameters): cats.data.Reader[$genericTypeName, $className] = {
           cats.data.Reader { r =>
             ..$readValues
             new ${TypeName(klassName)}(...${List(values)})
           }
         }
       """
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a class, found $other")

    }

  }

}

class reader extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReaderMacro.impl
}
