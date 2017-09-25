package org.zalando.grafter

import org.specs2.Specification
import org.specs2.execute.Result
import org.specs2.matcher.ThrownExpectations
import org.zalando.grafter.Visualize._
import org.zalando.grafter.visualize.Foo
import org.zalando.grafter.syntax.visualize._

class VisualizeSpec extends Specification with ThrownExpectations { def is = s2"""

 The example graph must be correctly serialized into .dot format $create

 A package filter can be used to only keep specified classes in the resulting graph $filter1
 Included nodes are kept including their parents                                    $filter2
 Excluded nodes are removed including their children                                $filter3

 Nodes can display more details
   with their attributes  $display1
   or with a node summary $display2
   or not at all          $display3

"""
  import Graph._

  def create = Result.foreach(1 to 1000) { i =>

    val a = A()
    val b1 = B(a)
    val b2 = B(a)
    val c1 = C(a, b1, b2)
    val c2 = C(a, b1, b2)
    val app = D(c1, c2)

    app.asDotString(display = None) ====
      s"""|strict digraph {
          |  node [shape=box];
          |  "A";
          |  "B # 1/2";
          |  "B # 2/2";
          |  "C # 1/2";
          |  "C # 2/2";
          |  "D";
          |  "B # 1/2" -> "A";
          |  "B # 2/2" -> "A";
          |  "C # 1/2" -> "A";
          |  "C # 1/2" -> "B # 1/2";
          |  "C # 1/2" -> "B # 2/2";
          |  "C # 2/2" -> "A";
          |  "C # 2/2" -> "B # 1/2";
          |  "C # 2/2" -> "B # 2/2";
          |  "D" -> "C # 1/2";
          |  "D" -> "C # 2/2";
          |}""".stripMargin
  }

  def filter1 = {

    val app = E(A(), Foo())

    val included = Visualize.packageFilter(includePackages = "org.zalando.grafter".r, excludePackages = Some("org.zalando.grafter.visualize".r))
    val dot = app.asDotString(included, display = None)

    dot ====
      s"""|strict digraph {
          |  node [shape=box];
          |  "A";
          |  "E";
          |  "E" -> "A";
          |}""".stripMargin
  }

  def filter2 = {

    val app = J(E(A(), Foo()), F(A(), G("name")))

    val included = (p: Product) => {
      val className = p.getClass.getSimpleName
      className.contains("A") || className.contains("G")
    }
    val dot = app.asDotString(included, display = None)

    dot ====
      s"""|strict digraph {
          |  node [shape=box];
          |  "A # 1/2";
          |  "A # 2/2";
          |  "J";
          |  "J" -> "A # 1/2";
          |  "J" -> "A # 2/2";
          |}""".stripMargin
  }

  def filter3 = {

    val app = J(E(A(), Foo()), F(A(), G("name")))

    val included = (p: Product) => {
      val className = p.getClass.getSimpleName
      className.contains("A") || className.contains("G")
    }
    val excluded = (p: Any) => p.getClass.getSimpleName.contains("G")
    val dot = app.asDotString(included, excluded, display = None)

    dot ====
      s"""|strict digraph {
          |  node [shape=box];
          |  "A # 1/2";
          |  "A # 2/2";
          |  "J";
          |  "J" -> "A # 1/2";
          |  "J" -> "A # 2/2";
          |}""".stripMargin
  }

  def display1 = {

    val app = I(A(), h)

    val dot = app.asDotString

    dot ====
      s"""|strict digraph {
          |  node [shape=record];
          |  "A";
          |  "H" [label = "{H|+ anyVal=g\\l+ a=1\\l+ b=boo\\l+ c=2.0\\l+ d=a\\l+ e=true\\l+ f=1.0\\l+ g=-32768\\l+ h=-128\\l}"];
          |  "I";
          |  "I" -> "A";
          |  "I" -> "H";
          |}""".stripMargin
  }

  def display2 = {

    val app = I(A(), h)

    val dot = app.asDotString(display = Some(nodeDisplay.copy(summary = (p: Product) =>
      p match {
        case h: H => Some(h.anyVal.name)
        case _ => None
      })))

    dot ====
      s"""|strict digraph {
          |  node [shape=record];
          |  "A";
          |  "H" [label = "{H|g\\l}"];
          |  "I";
          |  "I" -> "A";
          |  "I" -> "H";
          |}""".stripMargin
  }

  def display3 = {

    val app = F(A(), G("name"))

    val dot = app.asDotString(display = None)

    dot ====
      s"""|strict digraph {
          |  node [shape=box];
          |  "A";
          |  "F";
          |  "F" -> "A";
          |}""".stripMargin
  }
}

object Graph {

  case class A()
  case class B(a: A)
  case class C(a: A, b1: B, b2: B)
  case class D(c1: C, c2: C)
  case class E(a: A, foo: Foo)
  case class F(a: A, g: G)
  case class G(name: String) extends AnyVal
  case class H(anyVal: G, a: Int, b: String, c: Double, d: Char, e: Boolean, f: Float, g: Short, h: Byte)
  val h = H(G("g"), a = 1, b = "boo", c = 2.0, d = 'a', e = true, f = 1.0f, g = Short.MinValue, h = Byte.MinValue)

  case class I(a: A, h: H)
  case class J(e: E, f: F)
}
