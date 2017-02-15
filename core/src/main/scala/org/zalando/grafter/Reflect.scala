package org.zalando.grafter

import scala.reflect.ClassTag

trait Reflect { outer =>

  /**
   * @return true if A implements the list of types defined by a given class tag
   */
  def implements(a: Any)(implicit ct: ClassTag[_]): Boolean = {
    val types: List[Class[_]] =
      ct.runtimeClass +: ct.runtimeClass.getInterfaces.toList

    types.forall(t => t.isAssignableFrom(a.getClass))
  }

  implicit class ReflectOps(t: Any) {
    def implements[T : ClassTag]: Boolean =
      outer.implements(t)(implicitly[ClassTag[T]])
  }
}

object Reflect extends Reflect
