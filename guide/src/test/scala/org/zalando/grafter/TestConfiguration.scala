package org.zalando.grafter

object TestConfiguration extends UserGuidePage { def is = "Test the configuration".title ^ s2"""

In the page about ${"making singletons" ~/ CreateSingletons} we see that we can modify the configuration of some components
with `modifyWith` and create a limited number of instances for some components.

It is highly recommended that you add tests to check that the modification you intend to make on your graph really
happen as you wish.

### Collecting ancestors

For example we can collect all the components using a specific component type: ${snippet{
// 8<--
case class HttpClient()
val application = Nil
// 8<--
import org.zalando.grafter.syntax.query._

val usersOfHttpClient: Map[HttpClient, List[List[Any]]] =
  application.ancestors[HttpClient]
}}

If we take the example shown in ${"making singletons" ~/ CreateSingletons}, we can then make sure that we end up with a
`Map` containing one `HttpClient` used by the `CustomerService` and a different `HttpClient` used by the `PriceService`.

### Visual inspection

A quick and useful way to check the state of your application is to create the corresponding graph: ${snippet{
// 8<--
case class HttpClient()
object Application { def prod = Nil }
// 8<--
import org.zalando.grafter.syntax.visualize._

val application = Application.prod

application.asDotString ====
  s"""
  |strict digraph {
  |  node [shape=record]
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
}}

`asDotString` produces a `.dot` graph which you can visualize with [webgraphviz](http://www.webgraphviz.com) or similar tools.

![](images/webgraphviz-example.png)

##### Configuration

You can configure the generation of the `dot` graph by passing to the `asDotString` method:

 - `included: Product => Boolean` to describe which nodes should be kept, those nodes will be kept even if their parents are
 being filtered out

 - `excluded: Any => Boolean` to describe which nodes should be excluded including their children

 - `display: NodeDisplay(summary, attributesFilter)` to show more details for a given node
     - `summary: Product => Option[String]`. This function can be used to return a "summary" of a node to be displayed in a
        box below the node name. The default is `_ => None`.

     - `attributesFilter: Any => Option[Any]`. If `summary` doesn't return a result, this function is called for every
        of the product attributes. By default only "primitive" values (`String`, `Int`, `AnyVal`,...) values are being shown
<p/>

### With specs2

If you use [**specs2**](http://specs2.org) you can use the `org.zalando.grafter.specs2.matcher.ComponentsMatchers` trait
to check the number of components of a given type in your application:${snippet{
import org.zalando.grafter.specs2.matcher._
import org.specs2.Specification

class ApplicationSpec extends Specification with ComponentsMatchers { def is = s2"""

The application contains the right number of components $checkApplication

"""

  val application = Application()

  def checkApplication = {
    application must containInstances(
      classOf[Service1] -> 1,
      classOf[Service2] -> 1,
      classOf[Service3] -> 2
    )
  }

}

case class Application(service1: Service1 = Service1(), service2: Service2 = Service2())
case class Service1(service3: Service3 = Service3())
case class Service2(service3: Service3 = Service3())
case class Service3()
}}
"""
}
