package org.zalando.grafter.macros

import org.specs2.Specification

object ReaderMacroTest {
  val r1: cats.data.Reader[ApplicationConfig, C] = C.reader
}

class ReaderMacroSpec extends Specification { def is = s2"""

 the reader annotation can be used to declare a reader for a given config type $useAnnotation

"""

  def useAnnotation = {
    R1.reader.apply(ApplicationConfig()).r3.r4.s ==== "hello"
  }

}

case class ApplicationConfig()

@reader[ApplicationConfig]
case class C()

//This will not compile because the type parameter is missing
//@reader
//case class CC()

@reader[ApplicationConfig]
case class R1(r2: R2, r3: R3, r4: R4) {
  private val field1 = "should not be used for the reader instance"
}

@reader[ApplicationConfig]
case class R2()

@reader[ApplicationConfig]
case class R3(r4: R4)

case class R4(s: String)

object R4 {
  implicit def reader: cats.data.Reader[ApplicationConfig, R4] =
    cats.data.Reader(_ => R4("hello"))
}
