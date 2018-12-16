def call(Map<String, String> body ) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
 //   body.resolveStrategy = Closure.DELEGATE_FIRST
  //  body.delegate = pipelineParams
  //  body()
    pipelineParams = body

    pipeline {
        // our complete declarative pipeline can go in here
        pipeline {
            agent any
            stages {
                stage('checkout git') {
					steps{
					git branch: pipelineParams.BRANCH, url:'https://github.com/Sanjeev435/spring-petclinic.git'
					}
                        
                }

                stage('build') {
					steps{
					if(isUnix()){
                            sh 'mvn clean package -Dmaven.test.skip=true'
                        }
                        else{
                            bat('mvn clean install -Dmaven.test.skip=true')
                        }
					}
                    
                  }
                
                stage ('test') {
				steps{
				    if(pipelineParams.RUN_TEST == 'yes'){
						if(isUnix()){
							parallel (
								"unit tests": { sh 'mvn test' },
								"integration tests": { sh 'mvn integration-test' }
							)
						else{
							parallel (
								"unit tests": { bat('mvn test')},
								"integration tests": { bat('mvn integration-test')}
							)
						}
					}
				}

			}
		}
	
            post {
                failure {
                    mail to: pipelineParams.EMAIL, subject: 'Pipeline failed', body: "${env.BUILD_URL}"
                }
				success{
					println "SUCCESS"
				}
            }
        }
    }
}
