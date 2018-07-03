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
      case ClassDef(_, className, typeParams, Template(_, _, fields)) =>
        typeParams match {
          case Nil =>
            val params = removeDuplicatedTypes(c)(fieldsNamesAndTypes(c)(fields))

            val implicitReaders =
              params.map { case (fieldName, fieldType) =>
                val readerName = TermName(name(fieldName) + "Reader")

                c.Expr[Any] {
                  q"""
                    implicit def $readerName: cats.data.Reader[$className, $fieldType] =
                      cats.data.Reader(_.${TermName(name(fieldName))})

                  """
                }
              }

            val implicitTransitiveReaders =
              params.map { case (fieldName, fieldType) =>
                val transitiveReaderName = TermName("transitive"+name(fieldName) + "Reader")

                c.Expr[Any] {
                  q"""

                    implicit def $transitiveReaderName[A](implicit r1: cats.data.Reader[$fieldType, A], r2: cats.data.Reader[$className, $fieldType]): cats.data.Reader[$className, A] =
                      r2.map(a => r1(a))

                  """
                }
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
                ..$implicitTransitiveReaders

                ..$readerIdentity
              """
            }

          case tpe :: Nil =>
            tpe.tparams match {
              // if the class contains a type parameter T we generate specific
              // reader instances for the members of the form (A, T) where the reader
              // only returns A
              case Nil =>
                val params = removeDuplicatedTypes(c)(fieldsNamesAndTypes(c)(fields))
                val typeName = internal.reificationSupport.freshTypeName("A")

                val implicitReaders =
                  params.map { case (fieldName, fieldType) =>
                    val readerName = TermName(name(fieldName) + "Reader")

                    fieldType match {
                      case AppliedTypeTree(tp, t1 :: t2 :: Nil) if tpe.name.toString == t2.toString =>
                        c.Expr[Any] {
                          q"""
                            implicit def $readerName[$typeName]: cats.data.Reader[$className[$typeName], $t1] =
                              cats.data.Reader(_.${TermName(name(fieldName))}._1)
                          """
                        }

                      case _ =>
                        c.Expr[Any]{
                          q"""
                            implicit def $readerName[$typeName]: cats.data.Reader[$className[$typeName], $fieldType] =
                              cats.data.Reader(_.${TermName(name(fieldName))})
                          """
                        }
                    }
                  }

                def readerIdentity = c.Expr[Any] {
                  q"""
                      implicit def ${TermName(className.toString.uncapitalize+"Reader")}[$typeName]: cats.data.Reader[$className[$typeName], $className[$typeName]] =
                        cats.data.Reader(identity)
                  """
                }

                outputs(c)(classTree, className, companionTree) {
                  q"""
                    ..$implicitReaders

                    ..$readerIdentity
                  """
              }

              // if the class contains a higher order type parameter F[_] we generate
              // specific reader instances for the members of the form A[F].
              case _ :: Nil =>
                val params = removeDuplicatedTypes(c)(fieldsNamesAndTypes(c)(fields))

                val implicitReaders =
                  params.map { case (fieldName, fieldType) =>
                    val readerName = TermName(name(fieldName) + "Reader")

                    fieldType match {
                      case AppliedTypeTree(tp, t1 :: Nil) if t1.toString == tpe.name.toString =>
                        c.Expr[Any] {
                          q"""
                            implicit def $readerName[F[_]]: cats.data.Reader[$className[F], $tp[F]] =
                              cats.data.Reader(_.${TermName(name(fieldName))})
                          """
                        }

                      case _ =>
                        c.Expr[Any] {
                          q"""
                            implicit def $readerName: cats.data.Reader[$className[Nothing], $fieldType] =
                              cats.data.Reader(_.${TermName(name(fieldName))})
                          """
                        }
                    }
                  }

                def readerIdentity = c.Expr[Any] {
                  q"""
                    implicit def ${TermName(className.toString.uncapitalize+"Reader")}[F[_]]: cats.data.Reader[$className[F], $className[F]] =
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
                c.abort(c.macroApplication.pos, s"you can only use the @$annotationName annotation for a class having 0 or 1 type parameter, found $other")
            }

          case other =>
            c.abort(c.macroApplication.pos, s"you can only use the @$annotationName annotation for a class having 0 or 1 type parameter, found $other")
        }

      case other =>
        c.abort(c.macroApplication.pos, s"the @$annotationName annotation must annotate a class, found $other")
    }

  }

  implicit class StringOps(s: String) {
    def uncapitalize: String = s.take(1).map(_.toLower)++s.drop(1)
  }
}

class readers extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ReadersMacro.impl
}
