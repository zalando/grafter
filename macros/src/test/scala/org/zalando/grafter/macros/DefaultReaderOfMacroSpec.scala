package org.zalando.grafter.macros

import org.specs2.Specification
import org.specs2.matcher._
import org.specs2.execute._
import Typecheck._

class DefaultReaderOfMacroSpec extends Specification with TypecheckMatchers { def is = s2"""

 an annotation not placed on a trait must not compile $compilationError

"""

  def compilationError = {
    tc"""
       @defaultReaderOf[String, String]
       object O

    """ must failWith("the @defaultReaderOf annotation must annotate a trait, found object O")
  }

}
