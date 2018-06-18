package org.zalando.grafter.macros

import cats.data.Reader

object ReadersFMacroTest {

  val clientOptionReader: Reader[Config[Option], Client[Option]] =
    Config.clientReader[Option]

  val implicitClientOptionReader: Reader[Config[Option], Client[Option]] =
    implicitly[Reader[Config[Option], Client[Option]]]

  val nReader: Reader[Config[Nothing], Int] =
    Config.nReader

  val implicitNReader: Reader[Config[Nothing], Int] =
    implicitly[Reader[Config[Nothing], Int]]

}

@readers
case class Config[F[_]](n: Int, client: Client[F])

case class Client[F[_]]()
