
### Installation

To use Grafter you can add it as a dependency in your sbt build settings:

```scala
libraryDependencies += "org.zalando" %% "grafter" % "1.4.8"
```

Grafter also provides some annotations to help reducing the boilerplate in
your code. If you decide to use them, you need to add the following scala
compiler plugin to your sbt build:

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```
