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
  stage("Run tests") {
    unstash 'source'
    sh "/tools/run :sbt -- sbt clean test"
  }
}

if ("master".equals(env.BRANCH_NAME)) {
  node("kraken") {
    stage("Publish a new version") {
      unstash 'source'
      sh "/tools/run :sbt -- sbt publish"
    }
  }
}
