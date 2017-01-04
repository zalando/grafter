package org.zalando.grafter.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object ReaderMacro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    object TypeParamTraverser extends Traverser {
      var types: List[c.universe.TypeName] = List[TypeName]()
      override def traverse(tree: Tree): Unit = tree match {
        case New(AppliedTypeTree(Ident(TypeName("reader")), typeIds)) =>
          types = typeIds.collect {
            case Ident(typeName: TypeName) => typeName
          }
        case _ => super.traverse(tree)
      }
    }

    val inputs : (Tree, Tree, Option[Tree]) =
      annottees.toList match {
        case classDecl :: companion :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), Option(companion.tree))

        case classDecl :: rest =>
          (classDecl.tree, c.typecheck(classDecl.tree), None)

        case Nil => c.abort(c.enclosingPosition, "no target")
      }

    val outputs: List[Tree] = {
      val (original, ClassDef(_, className, _, _), companion) = inputs

      val genericReader = {
        TypeParamTraverser.traverse(c.macroApplication)
        TypeParamTraverser.types
          .headOption
          .map { a =>
            c.Expr[Any](
              q"""
               import org.zalando.grafter.GenericReader._
               implicit def reader: cats.data.Reader[$a, $className] = genericReader""")
          }
          .getOrElse {
            c.abort(c.enclosingPosition, "The @reader annotation requires a type parameter")
          }
      }

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

class reader[A] extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReaderMacro.impl
}
