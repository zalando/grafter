package org.zalando.grafter

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations

class StopSpec extends Specification with ThrownExpectations { def is = s2"""

 The Stop.success method returns true only for a StopOk $stopSuccess

 The Stop.eval method catches exceptions thrown during the stop of a component $evalStop
 The Stop.attempt method collects exceptions resulting of the stop of a component $attemptStop


"""

  def stopSuccess = {
    StopOk("m").success ==== true
    StopFailure("m").success ==== false
    StopError("m", exception).success ==== false
  }

  def evalStop = {
    StopResult.eval("component")(1).value ==== StopOk("component")
    StopResult.eval("component")(boom).value ==== StopError("component", exception)
  }

  def attemptStop = {
    StopResult.attempt("component")(Right(1)).value ==== StopOk("component")
    StopResult.attempt("component") { Left(exception) }.value ==== StopError("component", exception)
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
