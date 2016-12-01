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
  version in ThisBuild := "1.2.4"
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
  publishTo := Option("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  homepage := Some(url("https://github.com/zalando/grafter")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/zalando/grafter"), "scm:git:git@github.com:zalando/grafter.git")),
  autoAPIMappings := true,
  pomExtra := (
    <developers>
      <developer>
        <id>etorreborre</id>
        <name>Eric Torreborre</name>
        <url>https://github.com/etorreborre/</url>
      </developer>
    </developers>
    ),
  publishMavenStyle := true,
  publishArtifact in Test := false
) ++
  credentialSettings ++
  promulgateVersionSettings

lazy val credentialSettings = Seq(
  // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
)
