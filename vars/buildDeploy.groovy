def call(Map config) {
  node() {

    def appName = config.appName
    def gitProvider = config.gitProvider
    def appRepo = config.appRepo
    def deployNameSpace = config.deployNameSpace
    def repo = checkout scm
    def gitVersion = sh(returnStdout: true, script: 'git describe --tags --dirty=.dirty').trim()

    stage('Checkout repo') {
      git url: "git@${gitProvider}:${appRepo}/${appName}.git", branch: env.BRANCH_NAME
    }

    stage('Build') {
        sh "VERSION=${gitVersion} ./build.sh"
    }

    stage('Update deployment') {
      if (env.BRANCH_NAME == 'master') {
        sh "kubectl apply -f k8s.yaml"
        sh "kubectl set image deployment/${appName} ${appName}=${appRepo}/${appName}:${gitVersion} -n ${deployNameSpace}"
      }
    }
  }
}
