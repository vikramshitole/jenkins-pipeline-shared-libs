def call(Map config) {
  node() {

    timestamps {
      def appName = config.appName
      def environment = config.environment
      def gitVersion = config.gitVersion
      def registryAddress = config.registryAddress

      stage('Deploy microservice') {
        if (env.BRANCH_NAME == 'master') {
          echo "Deploying ${appName} version ${gitVersion}"
          build(
              job: 'deploy-microservice',
              parameters: [
                  string(name: 'MICROSERVICE', value: appName),
                  string(name: 'VERSION', value: gitVersion),
                  string(name: 'ENV', value: environment),
                  string(name: 'DOCKER_REPO', value: registry),
                  string(name: 'CONFIG_BRANCH', value: env.BRANCH_NAME)
              ]
          )
        }
      }
    }
  }
}
