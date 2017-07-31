package org.zalando.grafter.macros

import org.specs2.Specification
import org.specs2.matcher._
import org.specs2.execute._
import Typecheck._

class ReaderOfMacroSpec extends Specification with TypecheckMatchers { def is = s2"""

 the reader annotation can be used to declare a reader for a given config type $useAnnotation
 an annotation not placed on a class must not compile $compilationError

"""

  def useAnnotation = {
    R1.reader.apply(ApplicationConfig()).r3.r4.s ==== "hello"
  }

  def compilationError = {
    tc"""
       @readerOf[String]
       object O

    """ must failWith("the @readerOf annotation must annotate a class, found object O")
  }

}

object ReaderOfMacroTest {
  val r1: cats.data.Reader[ApplicationConfig, C] = C.reader
}

case class ApplicationConfig()

@readerOf[ApplicationConfig]
case class C()

//This will not compile because the type parameter is missing
//@reader
//case class CC()

@readerOf[ApplicationConfig]
case class R1(r2: R2, r3: R3, r4: R4) {
  private val field1 = "should not be used for the reader instance"
  println(field1)
}

@readerOf[ApplicationConfig]
case class R2()

@readerOf[ApplicationConfig]
case class R3(r4: R4)

case class R4(s: String)

object R4 {
  implicit def reader: cats.data.Reader[ApplicationConfig, R4] =
    cats.data.Reader(_ => R4("hello"))
}
