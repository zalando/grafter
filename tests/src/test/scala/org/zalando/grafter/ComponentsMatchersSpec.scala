package org.zalando.grafter

import org.specs2.Specification
import org.zalando.grafter.specs2.matcher._

class ComponentsMatchersSpec extends Specification with ComponentsMatchers { def is = s2"""

  A matcher can be used to check the contents of an application graph $useMatcher
  when there's a failure $showFailure

"""

  val application = Application()

  def useMatcher = {
    application must containInstances(
      classOf[Service1] -> 1,
      classOf[Service2] -> 1,
      classOf[Service3] -> 2
    )
  }

  def showFailure = {
    val result =
      application must containInstances(
        classOf[Service1] -> 1,
        classOf[Service4] -> 1,
        classOf[Service3] -> 1
      )

    result.message ====
      """|Application
         |is missing
         |  Service4 -> 1
         |doesn't have the right number of components for
         |  Service3 -> actual: 2, expected: 1""".stripMargin
  }
}

case class Application(service1: Service1 = Service1(), service2: Service2 = Service2())
case class Service1(service3: Service3 = Service3())
case class Service2(service3: Service3 = Service3())
case class Service3()
case class Service4()
