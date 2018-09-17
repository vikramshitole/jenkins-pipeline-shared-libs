def call(Map config) {
  node() {

    timestamps {
      def appName = config.appName
      def gitProvider = config.gitProvider
      def appRepo = config.appRepo
      def acrRepo = config.acrRepo
      def spClientId = config.spClientId
      def spClientSecret = config.spClientSecret
      def spTenantId = config.spTenantId
      def acrContainerRegistryName = config.acrRepo.tokenize('.')[0]
      def isDependencies = config.isDependencies ?: false
      def branchName = env.BRANCH_NAME ?: 'master'  // env.BRANCH_NAME is not set for non-multipipeline jobs

      stage('Checkout repo') {
        git url: "git@${gitProvider}:${appRepo}/${appName}.git", branch: env.BRANCH_NAME
      }

      def gitVersion = sh(returnStdout: true, script: 'git describe --tags --dirty=.dirty').trim()

      stage('ACR login') {
        sh  """#!/usr/bin/env bash
            set +x
            az login --service-principal --username ${spClientId} --password ${spClientSecret} --tenant ${spTenantId} || true
            """
        sh "az acr login --name ${acrContainerRegistryName} || true"
      }

      stage('Build image') {
        if (isDependencies) {
          sh "./build-deps.sh ${acrRepo}/${appName} dependencies"
        } else {
          sh "./build.sh ${acrRepo}/${appName} ${gitVersion}"
        }
      }

      stage('Push image') {
        if (env.BRANCH_NAME == 'master') {
          if (isDependencies) {
            sh "docker push ${acrRepo}/${appName}:dependencies"
          } else {
            sh "docker push ${acrRepo}/${appName}:${gitVersion}"
            sh "docker push ${acrRepo}/${appName}:latest"
          }
        }
      }
    }
  }
}
