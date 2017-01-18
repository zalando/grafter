package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object DependentReaderMacro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs : (Tree, Tree, Option[Tree]) =
      annottees.toList match {
        case classDecl :: companion :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), Option(companion.tree))

        case classDecl :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), None)

        case Nil => c.abort(c.enclosingPosition, "no target")
      }

    val outputs: List[Tree] = {
      val A = internal.reificationSupport.freshTypeName("A")
      val (original, ClassDef(_, className, _, Template(_, _, fields)), companion) = inputs

      val params = fields
        .collect { case ValDef(_, fieldName, fieldType, _) => (fieldName, fieldType) }
        .groupBy(_._2.tpe.typeSymbol.name.decodedName.toString)
        .values
        .map(_.head)
        .toList
        .map { case (fieldName, fieldType) =>
          val readerName = TermName(fieldName.toString.trim+"Reader")
          c.Expr[ValDef](q"""$readerName: Reader[$A, $fieldType]""")
        }

      val genericReader = c.Expr[Any](
        q"""
         import org.zalando.grafter.GenericReader._
         import cats.data.Reader
         implicit def dependentReader[$A](implicit ..$params): Reader[$A, $className] = genericReader""")

      val companionObject = companion match {
        case Some(q"""$mod object $companionName extends { ..$earlydefns } with ..$parents { ..$body }""") =>
          q"""$mod object $companionName extends { ..$earlydefns } with ..$parents {
           ..$body
           ..$genericReader
           }"""

        case None =>
          q"""object ${TermName(className.decodedName.toString)} {
           ..$genericReader
           }"""
      }

      original :: companionObject :: Nil
    }

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }

}

class dependentReader extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro DependentReaderMacro.impl
}
