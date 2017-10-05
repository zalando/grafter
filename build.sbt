import sbt.Keys._
import sbt._
import com.ambiata._

lazy val grafter = (project in file(".")).
  settings(
    rootSettings        ++
    compilationSettings ++
    commonSettings      ++
    publishSettings
  ).aggregate(core, macros)

lazy val core = (project in file("core")).
  settings(
    compilationSettings ++
    testSettings ++
    Seq(publishArtifact := false)
  )

lazy val macros = project.in(file("macros")).
  settings(
    compilationSettings ++
    Seq(publishArtifact := false)
  ).dependsOn(core)

lazy val examples = (project in file("examples")).
  settings(
    compilationSettings ++
      testSettings ++
      Seq(publishArtifact := false)
  ).dependsOn(core, macros)

lazy val tests = (project in file("tests")).
  settings(
    compilationSettings ++
      testSettings ++
      Seq(publishArtifact := false)
  ).dependsOn(core, core % "test->test", macros)

lazy val rootSettings = Seq(
  unmanagedSourceDirectories in Compile := unmanagedSourceDirectories.all(aggregateCompile).value.flatten,
  unmanagedSourceDirectories in Test    := unmanagedSourceDirectories.all(aggregateTest).value.flatten,
  sources in Compile                    := sources.all(aggregateCompile).value.flatten,
  sources in Test                       := sources.all(aggregateTest).value.flatten,
  mappings in (Test, packageBin)        ~= (_.filter(mappingFilter)),
  libraryDependencies                   := libraryDependencies.all(aggregateCompile).value.flatten
)

def mappingFilter(mapping: (File, String)): Boolean =
  mapping._1.getPath.contains("org/zalando/grafter/specs2")

lazy val aggregateCompile = ScopeFilter(
  inProjects(core, macros),
  inConfigurations(Compile))

lazy val aggregateTest = ScopeFilter(
  inProjects(core, macros, tests, examples),
  inConfigurations(Test))

lazy val commonSettings = Seq(
  organization         := "org.zalando",
  name                 := "grafter",
  version in ThisBuild := "2.2.2"
)

lazy val testSettings = Seq(
  fork          in Test := true,
  scalacOptions in Test ++= Seq("-Yrangepos"),
  testFrameworks in Test := Seq(TestFrameworks.Specs2),
  testOptions in Test += Tests.Filter(s => !s.endsWith("Specification")),
  coverageEnabled := false
)

lazy val compilationSettings = Seq(
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", scalaVersion.value),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
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
    // to debug the macros
    //"-Ymacro-debug-lite",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding", "UTF-8"
  )
)

lazy val publishSettings = Seq(
  publishTo := Option("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  publishMavenStyle := true,
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
  publishArtifact in (Test, packageBin) := true,
  publishArtifact in (Test, packageDoc) := true,
  publishArtifact in (Test, packageSrc) := true
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

shellPrompt in ThisBuild := { state =>
  val name = Project.extract(state).currentRef.project
  (if (name == "grafter") "" else name) + "> "
}
