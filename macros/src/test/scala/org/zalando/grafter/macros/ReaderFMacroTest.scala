package org.zalando.grafter.macros

import cats.Monad
import cats.implicits._

object ReaderFMacroTest {
  @readers
  case class Config(n: Int)

  val rc0: cats.data.Reader[Config, FinallyTaglessDefault[Option]] =
    implicitly[cats.data.Reader[Config, FinallyTaglessDefault[Option]]]

  val rc1: cats.data.Reader[Config, FT1[Option]] =
    implicitly[cats.data.Reader[Config, FT1[Option]]]

}

@defaultReader[FinallyTaglessDefault]
trait FinallyTagless[F[_]] {
  def getIt(id: Int): F[Option[String]]
}

@reader
case class FinallyTaglessDefault[F[_]](ft1: FT1[F], ft2: FT2[F])(implicit val m: Monad[F]) extends FinallyTagless[F] {
  def getIt(id: Int): F[Option[String]] =
    if (id % 2 == 0) ft1.getEven(id)
    else ft2.getOdd(id)
}

@reader
case class FT1[F[_]](n: Int)(implicit val m: Monad[F]) {
  def getEven(id: Int): F[Option[String]] =
    Monad[F].pure(None)
}

@reader
case class FT2[F[_]]()(implicit val m: Monad[F]) {
  def getOdd(id: Int): F[Option[String]] =
    Monad[F].pure(None)
}
