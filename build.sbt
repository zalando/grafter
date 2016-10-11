
name := "conf"
scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation:false",
  "-Xfatal-warnings",
  "-Xcheckinit",
  "-Xlint",
  "-Xlint:-nullary-unit",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-Ywarn-dead-code",
  "-Yno-adapted-args",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8"
)

scalacOptions in Test ++= Seq(
  "-Yrangepos"
)

testFrameworks in Test := Seq(TestFrameworks.Specs2)
