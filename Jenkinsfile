#!groovy

properties([
    pipelineTriggers([
      [$class: "GitHubPushTrigger"]
    ])
  ])

def sh = { cmd ->
	wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
		sh cmd
	}
}

node("kraken") {
  stage("Checkout") {
    deleteDir()
    checkout scm
    stash name: 'source'
  }
  stage("Run tests and publish test results") {
    unstash 'source'
    sh '/tools/run :sbt -- sbt ";clean;testOnly * -- console junitxml;coverage;coverageReport"'
    junit 'target/test-reports/*.xml'
  }
}

if ("master".equals(env.BRANCH_NAME)) {
  node("kraken") {
    stage("Publish a new version") {
      checkout scm
      sh "/tools/run :sbt -- sbt clean publish"
    }
  }
}
