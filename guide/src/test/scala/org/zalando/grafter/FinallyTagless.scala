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
case class DatabaseUserOperations[F[_]](config: DbConfig)(implicit val m: Monad[F]) extends UserOperations[F] {

  def getUser(name: String): F[User] = ???
  def createUser(name: String): F[User] = ???

}
}}

As you can see there is a minor drawback because you would probably like to write `case class DatabaseUserOperations[F[_] : Monad]`.
This is not currently possible because of [this Scala bug](https://github.com/scala/bug/issues/10589). Please vote for it
or even better propose a PR on the Scala project!

"""
}
