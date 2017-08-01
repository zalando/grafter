package org.zalando.grafter.macros

import org.specs2.Specification
import org.specs2.matcher._
import org.specs2.execute._
import Typecheck._

class ReadersMacroSpec extends Specification with TypecheckMatchers { def is = s2"""

 an annotation not placed on a class must not compile $compilationError

"""

  def compilationError = ok
//  {
//    tc"""
//       @readers
//       object O
//
//    """ must failWith("the @readers annotation must annotate a class, found object O")
//  }

}
