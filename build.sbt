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

scalaVersion in ThisBuild := currentScalaVersion

lazy val currentScalaVersion = "2.11.8"

lazy val root = (project in file(".")).
  aggregate(core)

lazy val core = (project in file("core")).
  settings(coreSettings: _*)

lazy val coreSettings = Seq(
  name := "grafter"
) ++ commonSettings

lazy val commonSettings = Seq(
  organization := "org.zalando",
  scalaVersion := currentScalaVersion
)
