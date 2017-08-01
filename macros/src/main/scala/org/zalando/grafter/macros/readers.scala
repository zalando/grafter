package org.zalando.grafter.macros

import org.zalando.grafter.macros.ReaderMacros.annotatedClass

class readers extends scala.macros.MacroAnnotation {

  def apply(defn: Any): Any = macro {
    val (classDef, objectDef) = annotatedClass("readers")(defn)
    ReadersMacro.expand(classDef, objectDef)
  }

}

