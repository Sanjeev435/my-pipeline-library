
def call(Map<String, String> ) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
 //   body.resolveStrategy = Closure.DELEGATE_FIRST
  //  body.delegate = pipelineParams
  //  body()

    pipeline {
        // our complete declarative pipeline can go in here
        pipeline {
            agent any
            stages {
                stage('checkout git') {
                    steps {
                        git branch: pipelineParams.branch, credentialsId: 'GitCredentials', url: pipelineParams.scmUrl
                    }
                }

                stage('build') {
                    steps {
                        sh 'mvn clean package -DskipTests=true'
                    }
                }

                stage ('test') {
                    steps {
                        parallel (
                                "unit tests": { sh 'mvn test' },
                                "integration tests": { sh 'mvn integration-test' }
                        )
                    }
                }

                stage('deploy developmentServer'){
                    steps {
                        deploy(pipelineParams.developmentServer, pipelineParams.serverPort)
                    }
                }

                stage('deploy staging'){
                    steps {
                        deploy(pipelineParams.stagingServer, pipelineParams.serverPort)
                    }
                }

                stage('deploy production'){
                    steps {
                        deploy(pipelineParams.productionServer, pipelineParams.serverPort)
                    }
                }
            }
            post {
                failure {
                    mail to: pipelineParams.email, subject: 'Pipeline failed', body: "${env.BUILD_URL}"
                }
            }
        }
    }
}
