package org.zalando.grafter.macros

import ReaderMacros._

object ReadersMacro {
  import scala.meta._

  def expand(classDef: Defn.Class, objectDef: Option[Defn.Object]): Term.Block = {

    classDef match {
      case Defn.Class(_, className, _, Ctor.Primary(_, _, paramss), _) =>
        paramss match {
          case params :: _ =>
            val implicitReaders =
              collectParamTypesAndNames(params).toList.map {
                case (paramType, paramName) =>
                  val readerName = Pat.Var(Term.Name(paramName.syntax + "Reader"))

                  q"""
                  implicit val $readerName: cats.data.Reader[$className, $paramType] =
                    cats.data.Reader(_.$paramName)
                """
              }

        def readerIdentity =

            q"""
                implicit val ${Pat.Var(Term.Name(className.syntax.uncapitalize+"Reader"))}: cats.data.Reader[$className, $className] ={
                  cats.data.Reader(identity)
             }
          """

          output(classDef, objectDef)(readerIdentity +: implicitReaders:_*)

          case Nil =>
            output(classDef, objectDef)()
      }

    }
  }

}

class readers extends scala.macros.MacroAnnotation {

  def apply(defn: Any): Any = macro {
    val (classDef, objectDef) = annotatedClass("readers")(defn)
    ReadersMacro.expand(classDef, objectDef).asInstanceOf[scala.macros.Term]
  }

}
