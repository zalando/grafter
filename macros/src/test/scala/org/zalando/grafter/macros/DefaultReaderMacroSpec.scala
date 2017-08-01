package org.zalando.grafter.macros

import org.specs2.Specification
import org.specs2.execute.Typecheck._
import org.specs2.matcher._

class DefaultReaderMacroSpec extends Specification with TypecheckMatchers { def is = s2"""

 an annotation not placed on a trait must not compile $compilationError

"""

  def compilationError = {
    tc"""
       @defaultReader[String]
       object O

    """ must failWith("the @defaultReader annotation must annotate a trait, found object O")
  }

}
