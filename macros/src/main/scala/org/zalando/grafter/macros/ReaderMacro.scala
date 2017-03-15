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
          (classDecl.tree, classDecl.tree, Option(companion.tree))

        case classDecl :: rest =>
          (classDecl.tree, classDecl.tree, None)

        case Nil => c.abort(c.enclosingPosition, "no target")
      }

    val outputs: List[Tree] = {
      val (original, ClassDef(_, className, _, Template(_, _, fields)), companion) = inputs


      def params = fields.collect { case ValDef(mods, fieldName, fieldType, _) if mods.hasFlag(Flag.CASEACCESSOR) =>
        (fieldName, fieldType)
      }

      val typeParam = {
        TypeParamTraverser.traverse(c.macroApplication)
        TypeParamTraverser.types.headOption.get
      }

      val klassName  = className.decodedName.toString
      val implicits  = params.map { p => q"""private val ${TermName("_"+p._1)}: cats.data.Reader[$typeParam, ${p._2}] = implicitly[cats.data.Reader[$typeParam, ${p._2}]];""" }
      val readValues = params.map(_._1).map { p => q"""val ${TermName("_"+p+"Value")} = ${TermName("_"+p)}.apply(r);""" }
      val values     = List(params.map(_._1).map { p => q"""${TermName("_"+p+"Value")}""" })

      val reader = {
        TypeParamTraverser.traverse(c.macroApplication)
        TypeParamTraverser.types.headOption.map { a =>
          c.Expr[Any](
              q"""
               implicit val reader: cats.data.Reader[$typeParam, ${TypeName(klassName)}] = {
                 cats.data.Reader { r =>
                   ..$readValues
                   new ${TypeName(klassName)}(...$values)
                 }
               }
               """)
          }
          .getOrElse {
            c.abort(c.enclosingPosition, "The @reader annotation requires a type parameter")
          }
      }

      val companionObject = companion match {
        case Some(q"""$mod object $companionName extends { ..$earlydefns } with ..$parents { ..$body }""") =>
          q"""$mod object $companionName extends { ..$earlydefns } with ..$parents {
           ..$body

           ..$implicits

           ..$reader
           }"""

        case None =>
          q"""object ${TermName(className.decodedName.toString)} {
           ..$implicits

           ..$reader
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
