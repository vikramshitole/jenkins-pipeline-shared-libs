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
      def gitVersion = sh(returnStdout: true, script: 'git describe --tags --dirty=.dirty').trim()

      stage('Checkout repo') {
        git url: "git@${gitProvider}:${appRepo}/${appName}.git", branch: env.BRANCH_NAME
      }

      stage('ACR login') {
        sh  """#!/usr/bin/env bash
            set +x
            az login --service-principal --username ${spClientId} --password ${spClientSecret} --tenant ${spTenantId} || true
            """
        sh "az acr login --name ${acrContainerRegistryName} || true"
      }

      stage('Build image') {
        sh "./build.sh ${acrRepo}/${appName} ${gitVersion}"
      }

      stage('Push image') {
        if (env.BRANCH_NAME == 'master') {
          sh "docker push ${acrRepo}/${appName}:${gitVersion}"
          sh "docker push ${acrRepo}/${appName}:latest"
        }
      }
    }
  }
}
