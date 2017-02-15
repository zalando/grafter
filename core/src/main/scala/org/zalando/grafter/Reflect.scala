package org.zalando.grafter

import scala.reflect.ClassTag

trait Reflect {

  /**
   * @return true if A implements the list of types defined by a given class tag
   */
  def implements(a: Any)(implicit ct: ClassTag[_]): Boolean = {
    val types: List[Class[_]] =
      ct.runtimeClass +: ct.runtimeClass.getInterfaces.toList

    types.forall(t => t.isAssignableFrom(a.getClass))
  }
}

object Reflect extends Reflect
