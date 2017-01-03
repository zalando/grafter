package org.zalando.grafter.macros

object ReaderMacroTest {
  val r1: cats.data.Reader[ApplicationConfig, C] = C.reader
}

case class ApplicationConfig()

@reader[ApplicationConfig]
case class C()

// This will not compile because the type parameter is missing
//@reader
//case class CC()