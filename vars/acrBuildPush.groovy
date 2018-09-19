def call(Map config) {
  node() {

    timestamps {
      def appName = config.appName
      def gitProvider = config.gitProvider
      def appRepo = config.appRepo
      def registryAddress = config.registryAddress
      def registryName = config.registryAddress.tokenize('.')[0]
      def spClientId = config.spClientId
      def spClientSecret = config.spClientSecret
      def spTenantId = config.spTenantId
      def isDependencies = config.isDependencies ?: false
      def branchName = env.BRANCH_NAME ?: 'master'  // env.BRANCH_NAME is not set for non-multipipeline jobs
      def repo = checkout scm
      def gitVersion = sh(returnStdout: true, script: 'git describe --tags --dirty=.dirty').trim()

      stage('Checkout repo') {
        git url: "git@${gitProvider}:${appRepo}/${appName}.git", branch: branchName
      }

      stage('ACR login') {
        sh  """#!/usr/bin/env bash
            set +x
            az login --service-principal --username ${spClientId} --password ${spClientSecret} --tenant ${spTenantId} || true
            """
        sh "az acr login --name ${registryName} || true"
      }

      stage('Build image') {
        if (isDependencies) {
          sh "./build-deps.sh ${registryAddress}/${appName}"
        } else {
          sh "./build.sh ${registryAddress}/${appName} ${gitVersion}"
        }
      }

      stage('Push image') {
        if (env.BRANCH_NAME == 'master') {
          if (isDependencies) {
            sh "docker push ${registryAddress}/${appName}:dependencies"
          } else {
            sh "docker push ${registryAddress}/${appName}:${gitVersion}"
            sh "docker push ${registryAddress}/${appName}:latest"
          }
        }
      }
      env.GIT_VERSION = gitVersion
    }
  }
}
