package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import ReaderMacros._

/**
 * This macro extracts all the fields of a component and builds a Reader[A, ComponentType] instance
 * requiring an implicit reader instance for each field type where A is the type referred by the readerOf[A]
 * annotation
 */
object ReaderOfMacro {

  val annotationName = "readerOf"

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def name(t: Name) = t.decodedName.toString.trim

    val (classTree, companionTree): (Tree, Option[Tree]) =
      annotationInputs(c)(annotationName)(annottees)

    classTree match {

      case ClassDef(_, className, _, Template(_, _, fields)) =>
        val typeParam  = typeParameter(annotationName)(c)
        def params     = fieldsNamesAndTypes(c)(fields)
        val paramNames = params.map(_._1).distinct
        val implicits  = params.map { case (p, _type) => q"""private val ${TermName("_"+name(p))}: cats.data.Reader[$typeParam, ${_type}] = implicitly[cats.data.Reader[$typeParam, ${_type}]];""" }
        val readValues = paramNames.map { p => q"""val ${TermName("_"+name(p)+"Value")} = ${TermName("_"+name(p))}.apply(r);""" }
        val values     = paramNames.map { p => q"""${TermName("_"+name(p)+"Value")}""" }
        val klassName  = name(className)

        val reader =
          c.Expr[Any](
            q"""
             implicit val reader: cats.data.Reader[$typeParam, ${TypeName(klassName)}] = {
               cats.data.Reader { r =>
                 ..$readValues
                 new ${TypeName(klassName)}(...${List(values)})
               }
             }
             """)

        outputs(c)(classTree, className, companionTree) {
          q"""
         ..$implicits

         ..$reader
      """
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a class, found $other")
    }

  }

}

class readerOf[A] extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReaderOfMacro.impl
}
