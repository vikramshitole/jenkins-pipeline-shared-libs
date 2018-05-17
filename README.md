# jenkins-pipeline-shared-libs
Shared library for creating simpler Jenkinsfiles

## Usage
Add the following line to the top of your Jenkinsfile

```
@Library('github.com/microdc/jenkins-pipeline-shared-libs@master') _
```

Alternate branchs/tags/commit shas can be specified after the @

## Functions
---
#### buildDeploy
1. Checkout a repo
1. build a container
1. deploy to kubernetes namespace.
Expects the repo to have git tags.

_example_
```
buildPromptTimeoutDeploy appName: 'k8s-jenkins',
                         gitProvider: 'github.com',
                         appRepo: 'microdc',
                         deployNameSpace: 'jenkins'
```
---
#### buildPromptTimeoutDeploy
Same as buildDeploy except this prompts the user for input when deploying and times out after 60s

---
#### ecrBuildPushDeploy
1. Checkout a repo
1. login to aws ecr
1. Build the container
1. push the container image to ecr
1. update kubernetes config with the latest version of the k8s.yaml from the source repo
1. update the pod image to the current build if we're building master

_example_
```
ecrBuildPushDeploy appName: 'someapp',
                   gitProvider: 'bitbucket.org',
                   appRepo: 'myveryownrepo',
                   deployNameSpace: 'apps',
                   ecrRepo: '111111111111.dkr.ecr.eu-west-1.amazonaws.com'
```
---
