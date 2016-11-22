package org.zalando.grafter

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations

class StartSpec extends Specification with ThrownExpectations { def is = s2"""

 The Start.success method returns true only for a StartOk $startSuccess

 The Start.eval method catches exceptions thrown during the start of a component $evalStart
 The Start.attempt method collects exceptions resulting of the start of a component $attemptStart

"""

  def startSuccess = {
    StartOk("m").success ==== true
    StartFailure("m").success ==== false
    StartError("m", exception).success ==== false
  }

  def evalStart = {
    StartResult.eval("component")(1).value ==== StartOk("component")
    StartResult.eval("component")(boom).value ==== StartError("component", exception)
  }

  def attemptStart = {
    StartResult.attempt("component")(Right(1)).value ==== StartOk("component")
    StartResult.attempt("component") { Left(exception) }.value ==== StartError("component", exception)
  }

  /**
   * HELPERS
   */

  val exception = new Exception("boom")

  def boom: Int = {
    if (1 + 1 == 2) throw exception
    else 1
  }
}
