package org.zalando.grafter

import org.specs2.Specification
import org.zalando.grafter.visualize.Foo
import org.zalando.grafter.syntax.visualize._

class VisualizeSpec extends Specification { def is = s2"""

 The example graph must be correctly serialized into .dot format $s1
 A package filter can be used to only keep specified classes in the resulting graph $s2

"""

  case class A()
  case class B(a: A)
  case class C(a: A, b1: B, b2: B)
  case class D(c1: C, c2: C)

  case class E(a: A, foo: Foo)

  def s1 = {

    val a = A()
    val b1 = B(a)
    val b2 = B(a)
    val c1 = C(a, b1, b2)
    val c2 = C(a, b1, b2)
    val app = D(c1, c2)

    app.asDotString ====
      s"""|strict digraph {
          |  "A" [shape=box];
          |  "B # 1/2" [shape=box];
          |  "B # 2/2" [shape=box];
          |  "C # 1/2" [shape=box];
          |  "C # 2/2" [shape=box];
          |  "D" [shape=box];
          |  "B # 1/2" -> "A"
          |  "B # 2/2" -> "A"
          |  "C # 1/2" -> "A"
          |  "C # 1/2" -> "B # 1/2"
          |  "C # 1/2" -> "B # 2/2"
          |  "C # 2/2" -> "A"
          |  "C # 2/2" -> "B # 1/2"
          |  "C # 2/2" -> "B # 2/2"
          |  "D" -> "C # 1/2"
          |  "D" -> "C # 2/2"
          |}""".stripMargin
  }

  def s2 = {

    val app = E(A(), Foo())

    val filter = Visualize.packageFilter(includePackages = "org.zalando.grafter".r, excludePackages = Some("org.zalando.grafter.visualize".r))
    val dot = app.asDotString(filter)


    dot ====
      s"""|strict digraph {
          |  "A" [shape=box];
          |  "E" [shape=box];
          |  "E" -> "A"
          |}""".stripMargin

  }

}
