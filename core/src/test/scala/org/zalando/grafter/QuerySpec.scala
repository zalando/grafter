package org.zalando.grafter

import org.specs2._
import ExampleGraph._
import org.specs2.matcher.ThrownExpectations
import syntax.all._

class QuerySpec extends Specification with ThrownExpectations { def is = s2"""

 Nodes of a given type can be collected in a graph $collectNodes

"""

  def collectNodes = {
    val e1 = E("e1")
    val (f1, f2) = (F1("f1"), F2("f2"))

    val graph =
      A(
        B(D("d1"), e1, f1),
        C(D("d2"), E("e2"), f2))

    graph.collect[F] ==== List(f1, f2)
    graph.singleton[E].collect[E] ==== List(e1)
  }


}
