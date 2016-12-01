lazy val catsVersion       = "0.8.0"
lazy val kiamaVersion      = "2.0.0"
lazy val specs2Version     = "3.8.5"
lazy val shapelessVersion  = "2.3.2"

libraryDependencies in Global ++=
  cats      ++
  kiama     ++
  shapeless ++
  reflect   ++
  specs2

lazy val kiama =
  Seq("org.bitbucket.inkytonik.kiama" %% "kiama" % kiamaVersion)

lazy val cats = Seq(
  "org.typelevel" %% "cats-kernel" % catsVersion,
  "org.typelevel" %% "cats-core"   % catsVersion
)

lazy val specs2 = Seq(
  "org.specs2" %% "specs2-core",
  "org.specs2" %% "specs2-scalacheck",
  "org.specs2" %% "specs2-cats",
  "org.specs2" %% "specs2-junit"
).map(_ % specs2Version % "test")

lazy val shapeless =
  Seq("com.chuusai" %% "shapeless" % shapelessVersion)

lazy val reflect = Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.8")
