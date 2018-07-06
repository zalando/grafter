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
      case ClassDef(_, className, typeParams, Template(_, _, fields)) =>
        val caseclassParameters = fieldsNamesAndTypes(c)(fields)
        val implicitParameters = implicitFieldsNamesAndTypes(c)(fields)

        val implicitReaderParameters =
          implicitParameters.map { case (fieldName, fieldType) =>
            val paramName = TermName(name(fieldName))
            c.Expr[ValDef](q"""$paramName: $fieldType""")
          }

        val readerImplicitParameters = caseclassParameters
          .map { case (fieldName, fieldType) =>
            val readerName = TermName(name(fieldName)+"Reader")
            c.Expr[ValDef](q"""$readerName: cats.data.Reader[$genericTypeName, $fieldType]""")
          } ++ implicitReaderParameters

        val paramNames = caseclassParameters.map(_._1)
        val readValues = paramNames.map { p => q"""val ${TermName("_"+name(p)+"Value")} = ${TermName(name(p)+"Reader")}.apply(r);""" }
        val values     = paramNames.map { p => q"""${TermName("_"+name(p)+"Value")}""" }
        val klassName  = name(className)

        typeParams match {
          case tp :: Nil =>

            // if there are no case class parameters we need to create a fake
            // "unit" parameter to avoid clashes with the "transitiveReader" implicit created by the @readers
            // annotation. In return the @returns annotation provides an implicit Reader[A, Unit]
            if (caseclassParameters.isEmpty)
              outputs(c)(classTree, className, companionTree) {
                q"""
                 implicit def reader[$genericTypeName, $tp](implicit unitReader: cats.data.Reader[$genericTypeName, Unit], ..$implicitReaderParameters): cats.data.Reader[$genericTypeName, $className[${tp.name}]] = {
                   cats.data.Reader { r =>
                     // to avoid unused value warning
                     if (true) unitReader else unitReader
                     new ${TypeName(klassName)}
                   }
                 }
               """
              }
            else
               outputs(c)(classTree, className, companionTree) {
               q"""
                 implicit def reader[$genericTypeName, $tp](implicit ..$readerImplicitParameters): cats.data.Reader[$genericTypeName, $className[${tp.name}]] = {
                   cats.data.Reader { r =>
                     ..$readValues
                     new ${TypeName(klassName)}(...${List(values)})
                   }
                 }
               """
            }

          case Nil =>
            outputs(c)(classTree, className, companionTree) {
              q"""
         implicit def reader[$genericTypeName](implicit ..$readerImplicitParameters): cats.data.Reader[$genericTypeName, $className] = {
           cats.data.Reader { r =>
             ..$readValues
             new ${TypeName(klassName)}(...${List(values)})
           }
         }
       """
            }

          case other =>
            c.abort(c.macroApplication.pos, s"you can only use the @$annotationName annotation for a class having 0 or 1 type parameter, found $other")
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a class, found $other")

    }

  }

}

class reader extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReaderMacro.impl
}
