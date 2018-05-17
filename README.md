# jenkins-pipeline-shared-libs
Shared library for creating simpler Jenkinsfiles

## Usage
Add the following line to the top of your Jenkinsfile

```
@Library('github.com/microdc/jenkins-pipeline-shared-libs@master') _
```

Alternate branchs/tags/commit shas can be specified after the @

## Functions
#### buildDeploy
Checkout a repo, build a container then deploy to kubernetes namespace. Expects the repo to have git tags.
_example_
```
buildPromptTimeoutDeploy appName: 'k8s-jenkins',
                         gitProvider: 'github.com',
                         appRepo: "microdc",
                         deployNameSpace: 'jenkins'
```
#### buildPromptTimeoutDeploy
Same buildDeploy except this prompts the user for input when deploying and times out after 60s
