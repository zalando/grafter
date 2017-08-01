package org.zalando.grafter.macros

import scala.macros._

object ReaderMacros {

  /** get the annotated class and, if available, companion object */
  def annotatedClass(name: String)(annotated: Any): (Defn.Class, Option[Defn.Object]) = {

    annotated match {
      case Term.Block(stats) =>
        stats match {
          case (classDef @ Defn.Class(_,_,_,_,_)) :: (companionDef @ Defn.Object(_,_,_)) :: _ =>
            (classDef.asInstanceOf[Defn.Class], Some(companionDef.asInstanceOf[Defn.Object]))

          case _ =>
            abort(s"the @$name annotation must annotate a class, no statements found")
        }

      case classDef @ Defn.Class(_,_,_,_,_) =>
        (classDef.asInstanceOf[Defn.Class], None)

      case other =>
        abort(s"the @$name annotation must annotate a class, found $other")
    }
  }

  /** get the annotated trait and, if available, companion object */
  def annotatedTrait(name: String)(annotated: Tree): (Defn.Trait, Option[Defn.Object]) =
    annotated match {
      case Term.Block(stats) =>
        stats match {
          case (traitDef @ Defn.Trait(_,_,_,_,_)) :: (companionDef @ Defn.Object(_,_,_)) :: _ =>
            (traitDef.asInstanceOf[Defn.Trait], Some(companionDef.asInstanceOf[Defn.Object]))

          case _ =>
            abort(s"the @$name annotation must annotate a trait, no statements found")
        }

      case traitDef @ Defn.Trait(_,_,_,_,_) =>
        (traitDef.asInstanceOf[Defn.Trait], None)

      case other =>
        abort(s"the @$name annotation must annotate a trait, found $other")
    }

  def output(defn: Defn with Member.Type, objectDef: Option[Defn.Object])(out: Stat*): Term  = {
    val name =
      defn match {
        case Defn.Class(_, n, _, _, _) => n
        case Defn.Trait(_, n, _, _, _) => n
      }

    val o = objectDef.getOrElse(q"object ${Term.Name(name.value)}")

    val extendedObject =
      o match {
        case Defn.Object(mods, oname, scala.macros.Template(early, inits, self, stats)) =>
          Defn.Object(mods, oname, scala.macros.Template(early, inits, self, stats ++ out.toList))
      }

    q"""
      $defn
      $extendedObject
    """
  }

  def collectParamTypesAndNames(params: Seq[Tree]): Map[Type.Name, Term.Name] =
    collectParamTypesAndNamesAsList(params).groupBy(_._1.value).map {
      case (typeName, termNames) => (Type.Name(typeName), termNames.head._2)
    }

  def collectParamTypesAndNamesAsList(params: Seq[Tree]): List[(Type.Name, Term.Name)] =
    params.toList.collect {
      case param"$paramName: $paramType" =>
        paramType match {
          case Type.Name(value) =>
            Option((Type.Name(value), Term.Name(paramName.syntax)))
          case _ =>
            None
        }
    }.flatten

  implicit class StringOps(s: String) {
    def uncapitalize: String =
      s.take(1).map(_.toLower)++s.drop(1)
  }

  def abort(message: String) =
    throw new scala.macros.internal.AbortMacroException(scala.macros.Position.None, message)
}
