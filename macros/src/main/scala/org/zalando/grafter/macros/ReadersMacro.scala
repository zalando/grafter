package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

object ReadersMacro {

  def impl(c: scala.reflect.macros.whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs : (Tree, Tree, Option[Tree]) =
      annottees.toList match {
        case classDecl :: companion :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), Option(companion.tree))

        case classDecl :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), None)

        case Nil => c.abort(c.enclosingPosition, "no target")
      }

    val outputs: List[Tree] = inputs match {

      case (original, ClassDef(_, className, _, Template(_, _, fields)), companion) =>
        def readerInstances =
          fields.
            collect { case field @ ValDef(_, fieldName, fieldType, _) => (fieldName, fieldType) }.
            groupBy(_._2.tpe.typeSymbol.name.decodedName.toString).values.map(_.head).toList.
            map { case (fieldName, fieldType) =>
              val readerName = TermName(fieldName.toString.trim+"Reader")
              val fieldAccessor = TermName(fieldName.toString.trim)

              c.Expr[Any](
            q"""
                implicit def $readerName: cats.data.Reader[$className, $fieldType] =
                  cats.data.Reader(_.$fieldAccessor)""")
            }

        val companionObject =
        companion match {
          case Some(q"""object $companionName { ..$body }""") =>
            q"""object $companionName {
           ..$body
           ..$readerInstances
           }"""

          case None =>
            q"""object ${TermName(className.decodedName.toString)} {
           ..$readerInstances
           }"""
        }
        original :: companionObject :: Nil

      case other => c.abort(c.enclosingPosition, "The @readers annotation can only annotate a simple case class with no extension or type parameters")
    }

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }

}

class readers extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReadersMacro.impl
}
