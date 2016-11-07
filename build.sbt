import sbt.Keys._
import sbt._
import com.ambiata._

lazy val grafter = (project in file(".")).
  settings(
    commonSettings      ++
    compilationSettings ++
    testSettings        ++
    publishSettings
  )

lazy val commonSettings = Seq(
  organization         := "org.zalando",
  name                 := "grafter",
  scalaVersion         := "2.11.8",
  version in ThisBuild := "1.2.1"
)

lazy val testSettings = Seq(
  fork          in Test := true,
  scalacOptions in Test ++= Seq("-Yrangepos"),
  testFrameworks in Test := Seq(TestFrameworks.Specs2),
  testOptions in Test += Tests.Filter(s => !s.endsWith("Specification")),
  coverageEnabled := false
)

lazy val compilationSettings = Seq(
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
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
)

lazy val publishSettings = Seq(
  publishTo := Option("zalando-releases" at "https://maven.zalando.net/content/repositories/releases"),
  publishMavenStyle := true
) ++
  promulgateVersionSettings
