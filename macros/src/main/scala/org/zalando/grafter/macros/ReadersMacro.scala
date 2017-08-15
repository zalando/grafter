package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import ReaderMacros._

object ReadersMacro {

  val annotationName = "readers"

  def impl(c: scala.reflect.macros.whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    def name(t: Name) = t.decodedName.toString.trim

    val (classTree, companionTree): (Tree, Option[Tree]) =
      annotationInputs(c)(annotationName)(annottees)

    classTree match {
      case ClassDef(_, className, _, Template(_, _, fields)) =>
        val params = removeDuplicatedTypes(c)(fieldsNamesAndTypes(c)(fields))

        val implicitReaders =
          params.map { case (fieldName, fieldType) =>
            val readerName = TermName(name(fieldName) + "Reader")

            c.Expr[Any](
              q"""
           implicit def $readerName: cats.data.Reader[$className, $fieldType] =
             cats.data.Reader(_.${TermName(name(fieldName))})""")
          }

        def readerIdentity = c.Expr[Any] {
          q"""
          implicit def ${TermName(className.toString.uncapitalize+"Reader")}: cats.data.Reader[$className, $className] =
            cats.data.Reader(identity)
      """
        }

        outputs(c)(classTree, className, companionTree) {
          q"""
         ..$implicitReaders

         ..$readerIdentity
      """
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a class, found $other")
    }


  }

  implicit class StringOps(s: String) {
    def uncapitalize: String =
      s.take(1).map(_.toLower)++s.drop(1)
  }
}

class readers extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReadersMacro.impl
}
