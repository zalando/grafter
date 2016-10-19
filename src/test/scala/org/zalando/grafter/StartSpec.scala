package org.zalando.grafter

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations

class StartSpec extends Specification with ThrownExpectations { def is = s2"""

 The Start.success method returns true only for a StartOk $e1

"""

  def e1 = {
    StartOk("m").success ==== true
    StartFailure("m").success ==== false
    StartError("m", new Exception("boom")).success ==== false
  }
}
