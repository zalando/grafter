package org.zalando.grafter

object FinallyTagless extends UserGuidePage { def is = "Finally tagless".title ^ s2"""

The ["finally tagless"](http://okmij.org/ftp/tagless-final/index.html) style for organising software functionalities is
more and more popular. Grafter supports this approach by allowing you to specify a type constructor as the type parameter
of a component: ${snippet{
// 8<--
  trait User
  trait DbConfig
// 8<--
import org.zalando.grafter.macros.{defaultReader, reader}
import cats.Monad

@defaultReader[DatabaseUserOperations]
trait UserOperations[F[_]] {
  def getUser(name: String): F[User]
  def createUser(name: String): F[User]
}

@reader
case class DatabaseUserOperations[F[_]](config: DbConfig)(implicit m: Monad[F]) extends UserOperations[F] {
  def getUser(name: String): F[User] = ???
  def createUser(name: String): F[User] = ???

}
}}

"""
}
