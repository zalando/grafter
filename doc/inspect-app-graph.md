### Inspect your application graph

It might be necessary to inspect (or test) the results of modifications on your application graph after several modifications with
`singleton` and `modify`.

For example you might want to collect all the distinct components of a given type:

```scala
import org.zalando.grafter.syntax.query._

val application = Application.prod.
  replace[HttpConfig](HttpConfig("localhost", 8080))

// this should contain the previously set http config
val httpConfigs: List[HttpConfig] =
  application.collect[HttpConfig]

```

You might also want to check what are all the components using a component of a given type:
```scala
val application = Application.prod.
  singleton[ExecutionService]

// this is a map where the keys are distinct instances of ExecutionService
// (there should be only one, since we made a singleton)
// and a list of all the paths from that key to the root
import org.zalando.grafter.syntax.query._

val usersOfExecutionService: Map[ExecutionService, List[List[Any]]] =
  application.ancestors[ExecutionService]
```

It is also very useful to be able to display a graph of your application with the `Visualize` functionality:

```scala
import org.zalando.grafter.syntax.visualize._

val application = Application.prod

application.asDotString ====
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
```

`asDotString` produces a `.dot` graph which you can visualize with [webgraphviz](http://www.webgraphviz.com) or similar tools

![webgraphviz](webgraphviz-example.png)

### With specs2

If you use [**specs2**](http://specs2.org) you can use the `org.zalando.grafter.specs2.matcher.ComponentsMatchers` trait
to check the number of components of a given type in your application:
```scala
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
``` 

----
Previous: [Testing](testing.md)
