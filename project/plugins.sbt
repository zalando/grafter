resolvers ++= Seq(
  Resolver.url("ambiata-oss", new URL("https://ambiata-oss.s3.amazonaws.com"))(Resolver.ivyStylePatterns),
  "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"
)

addSbtPlugin("com.ambiata" % "promulgate" % "0.11.0-20160104104535-e21b092")
