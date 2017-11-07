package org.zalando.grafter

object Comparisons extends UserGuidePage { def is = "Comparisons".title ^ s2"""

There are many libraries or approaches for doing [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) in Scala.

Libraries:

  - [MacWire](https://github.com/adamw/macwire)
  - [Guice](https://github.com/google/guice)
  - [Scaladi](https://github.com/scaldi/scaldi)

Design Patterns:

  - [Cake pattern](http://www.cakesolutions.net/teamblogs/2011/12/19/cake-pattern-in-depth)
<p/>

The main differences between Grafter and other libraries are the following:

 - Grafter components directly have `@reader` annotation which provide a way to "wire" them with their dependencies
 once an application configuration is available. Other libraries generally require a separate "module" to declare those
 "bindings"

 - in Grafter each interface needs to have at least a default implementation. This is not required with other libraries

 - there is no "interceptors" or "scoping" in Grafter. The only thing you can do is to `modify` or `replace` components

 - the "lifecycle" part (starting components) is completely separated from the "instantiation/configuration" part

"""
}
