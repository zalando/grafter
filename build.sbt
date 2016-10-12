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

lazy val currentScalaVersion = "2.11.8"

lazy val commonSettings = Seq(
  organization := "org.zalando",
  scalaVersion := currentScalaVersion
)

lazy val coreSettings = Seq(
  name := "conf4s"
) ++ commonSettings

lazy val core = (project in file("core")).
  settings(coreSettings: _*)

lazy val example = (project in file("example")).
  settings(commonSettings: _*).
  dependsOn(core)

lazy val root = (project in file(".")).
  aggregate(core, example)

scalaVersion in ThisBuild := currentScalaVersion
