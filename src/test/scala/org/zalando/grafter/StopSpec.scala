package org.zalando.grafter

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations

class StopSpec extends Specification with ThrownExpectations { def is = s2"""

 The Stop.success method returns true only for a StopOk $e1

"""

  def e1 = {
    StopOk("m").success ==== true
    StopFailure("m").success ==== false
    StopError("m", new Exception("boom")).success ==== false
  }
}
