def call(Map config) {
  node() {

    def appName = config.appName
    def gitProvider = config.gitProvider
    def appRepo = config.appRepo
    def deployNameSpace = config.deployNameSpace
    def ecrRepo = config.ecrRepo
    def repo = checkout scm
    def gitVersion = sh(returnStdout: true, script: 'git describe --tags --dirty=.dirty').trim()

    stage('Checkout repo') {
      git url: "git@${gitProvider}:${appRepo}/${appName}.git", branch: env.BRANCH_NAME
    }

    stage('ECR login') {
      sh "`aws ecr get-login --no-include-email`"
    }

    stage('Build image') {
      sh "./build.sh ${ecrRepo}/${appName} ${gitVersion}"
    }

    stage('Push image') {
      if (env.BRANCH_NAME == 'master') {
        sh "docker push ${ecrRepo}/${appName}:${gitVersion}"
        sh "docker push ${ecrRepo}/${appName}:latest"
      }
    }

    stage('Update kubernetes') {
      if (env.BRANCH_NAME == 'master') {
        sh "kubectl apply -f k8s.yaml"
        sh "kubectl set image deployment/${appName} ${appName}=${ecrRepo}/${appName}:${gitVersion} -n ${deployNameSpace}"
      }
    }
  }
}

