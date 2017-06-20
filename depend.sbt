
lazy val catsVersion       = "0.9.0"
lazy val kiamaVersion      = "2.1.0"
lazy val specs2Version     = "3.8.9"
lazy val shapelessVersion  = "2.3.2"

libraryDependencies in Global ++=
  cats      ++
  kiama     ++
  shapeless ++
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
  "org.specs2" %% "specs2-junit"
).map(_ % specs2Version % "test")

lazy val shapeless =
  Seq("com.chuusai" %% "shapeless" % shapelessVersion)

