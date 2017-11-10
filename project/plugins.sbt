resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.url("ambiata-oss", new URL("https://ambiata-oss.s3.amazonaws.com"))(Resolver.ivyStylePatterns),
  "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"
)

addSbtPlugin("com.ambiata"      % "promulgate"    % "0.11.0-20160104104535-e21b092")
addSbtPlugin("org.scoverage"    % "sbt-scoverage" % "1.5.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"       % "1.0.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"  % "1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages"   % "0.6.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-site"      % "1.3.1")
