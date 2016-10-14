package org.zalando.conf4s.env

import cats.data.Xor

trait FromStr[A] {
  def apply(str: String): Xor[FromStrError, A]
}

object FromStr {

  implicit val strFromStr: FromStr[String] = new FromStr[String] {
    def apply(str: String) = Xor.Right(str)
  }

  implicit val intFromStr: FromStr[Int] = new FromStr[Int] {
    def apply(str: String): Xor[FromStrError, Int] =
      Xor.catchNonFatal(str.toInt).
        leftMap(_ => FromStrError(s"Can not transform $str to Int"))
  }
}
