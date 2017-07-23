package org.zalando.grafter.macros

import scala.reflect.macros.whitebox

object ReaderMacros {

  /** get the annotated class and, if available, companion object */
  def annotationInputs(c: whitebox.Context)(name: String)(annottees: Seq[c.Expr[Any]]): (c.universe.Tree, Option[c.universe.Tree]) =
    annottees.toList match {
      case classDecl :: companion :: _ =>
      (classDecl.tree, Option(companion.tree))

      case classDecl :: _ =>
      (classDecl.tree, None)

      case Nil => c.abort(c.enclosingPosition, s"the @$name annotation must annotate a class")
    }

  /**
   * Inject an expression in the companion object of a class.
   *
   * Create one if necessary
   */
  def outputs(c: whitebox.Context)(
    classTree: c.universe.Tree,
    className: c.universe.TypeName,
    companionTree: Option[c.universe.Tree])(expression: c.universe.Tree) = {

    import c.universe._

    val insert = c.Expr[Any](expression)
    val out: List[Tree] = {
      val companionObject = companionTree match {
        case Some(q"""$mod object $companionName extends { ..$earlydefns } with ..$parents { ..$body }""") =>
          q"""$mod object $companionName extends { ..$earlydefns } with ..$parents {
           ..$body
           ..$insert
           }"""

        case None =>
          q"""object ${TermName(className.decodedName.toString)} {
           ..$insert
           }"""
      }

      classTree :: companionObject :: Nil
    }

    c.Expr[Any](q"..$out")
  }

  def fieldsNamesAndTypes(c: whitebox.Context)(fields: List[c.universe.Tree]): List[(c.universe.TermName, c.universe.Tree)] = {
    import c.universe._
    fields.collect { case ValDef(mods, fieldName, fieldType, _) if mods.hasFlag(Flag.CASEACCESSOR) =>
      (fieldName, fieldType)
    }
  }

  /**
   * return field names and types so that only one field per given type is present
   * For example if the fields are (server: ThreadPool, database: ThreadPool, port: Int)
   * only (server: ThreadPool, port: Int) is returned
   */
  def removeDuplicatedTypes(c: whitebox.Context)(params: List[(c.universe.TermName, c.universe.Tree)]): List[(c.universe.TermName, c.universe.Tree)] =
    params
      .groupBy(_._2.tpe.typeSymbol.name.decodedName.toString)
      .values.map(_.head).toList

  /** extract the type parameter of an annotation */
  def typeParameter(name: String)(c: whitebox.Context) = {
    import c.universe._
    val traverser = new Traverser {
      var types: List[c.universe.TypeName] = List[TypeName]()

      override def traverse(tree: Tree): Unit = tree match {
        case New(AppliedTypeTree(Ident(TypeName(_)), typeIds)) =>
          types = typeIds.collect {  case Ident(typeName: TypeName) => typeName }

        case _ =>
          super.traverse(tree)
      }
    }

    traverser.traverse(c.macroApplication)
    traverser.types.headOption match {
      case Some(t) => t
      case None    => c.abort(c.enclosingPosition, s"the @$name annotation requires a type parameter")
    }
  }

}
