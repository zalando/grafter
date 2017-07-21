package org.zalando.grafter

import shapeless._
import cats.data._

/**
 * The following functions are used to
 * derive reader instances for case classes
 * through the generic deriving mechanism from Shapeless
 *
 * See this blog post for more details: https://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation
 */
trait GenericReader {

  implicit def hnilReader[R]: Reader[R, HNil] =
    Reader(_ => HNil)

  implicit def hconsReader[R, H, T <: HList](implicit readerHead: Lazy[Reader[R, H]],
                                                      readerTail: Lazy[Reader[R, T]]
  ): Reader[R, H :: T] =
    Reader((r: R) => readerHead.value(r) :: readerTail.value(r))

  implicit def genericReader[R, A, Repr](implicit
    gen:  Generic.Aux[A, Repr],
    repr: Lazy[Reader[R, Repr]]
  ): Reader[R, A] =
    Reader((r: R) => gen.from(repr.value(r)))

  /**
   * this implicit conversion is useful to get contravariance for Reader instances
   * because cats' Reader is not contravariant and should be:
   *
   * If you have a Reader[C, Apple] you also have a Reader[C, Fruit] when Apple <: Fruit
   */
  implicit def widenReader[R, A, B](r: Reader[R, A])(implicit ev: A <:< B): Reader[R, B] =
    r.map(a => ev(a))

  /**
   * compose 2 readers instances where their implicit instances are available
   *
   * This is particularly useful when some components define their configuration in a distinct
   * library and you want to seamlessly extract that configuration from the application
   * configuration. See the "In a library" page in the User Guide
   */
  def composeReaders[C, T, S](implicit s: Reader[T, S], t: Reader[C, T] ): Reader[C, S] =
    t.map(t1 => s(t1))
}

object GenericReader extends GenericReader {

  def apply[R, A](implicit ev: Reader[R, A]): Reader[R, A] =
    ev

}
