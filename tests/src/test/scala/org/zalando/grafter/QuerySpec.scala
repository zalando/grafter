package org.zalando.grafter

import org.specs2._
import ExampleGraph._
import org.specs2.matcher.ThrownExpectations
import syntax.all._

class QuerySpec extends Specification with ThrownExpectations { def is = s2"""

 Nodes of a given type can be collected in a graph     $collectNodes
 The first node of a given type can be collected       $collectFirstNode
 Ancestors of a given type can be collected in a graph $collectAncestors

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

  def collectFirstNode = {
    val (e1, e2) = (E("e1"), E("e2"))
    val (f1, f2) = (F1("f1"), F1("f2"))
    case class G(e1: E, values: List[Any], f: F)

    val graph =
      G(e1, List(e2, f2), f1)

    // the search is breadth-first otherwise we would get f2
    graph.collectFirst[F] ==== Option(f1)
  }

  def collectAncestors = {
    val (e1, e2) = (E("e1"), E("e2"))
    val (f1, f2) = (F1("f1"), F2("f2"))
    val (d1, d2) = (D("d1"), D("d2"))

    val b = B(d1, e1, f1)
    val c = C(d2, e2, f2)
    val a = A(b, c)

    val graph = a

    // there are 2 f nodes, each of them has just one path to the root a
    graph.ancestors[F] ==== Map(f1 -> List(List(b, a)), f2 -> List(List(c, a)))

    // there is 1 e node after being made a singleton, it has 2 paths to the root a
    val c1 = C(d2, e1, f2)

    graph.singleton[E].ancestors[E] ==== Map(e1 ->
      List(List(b, A(b, c1)),
           List(c1, A(b, c1))))
  }


}
