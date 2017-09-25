package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import ReaderMacros._

object DefaultReaderMacro {

  val annotationName = "defaultReader"

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val (classTree, companionTree): (Tree, Option[Tree]) =
      annotationInputs(c)(annotationName)(annottees)

    classTree match {
      case ClassDef(_, className, _, _) =>
        val genericTypeName = internal.reificationSupport.freshTypeName("A")
        val typeParam = typeParameter(annotationName)(c)

        outputs(c)(classTree, className, companionTree) {
          q"""
         implicit def reader[$genericTypeName](implicit defaultReader: cats.data.Reader[$genericTypeName, $typeParam]): cats.data.Reader[$genericTypeName, $className] =
           org.zalando.grafter.GenericReader.widenReader(defaultReader)
      """
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a trait, found $other")
    }
  }

}

class defaultReader[A] extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DefaultReaderMacro.impl
}
