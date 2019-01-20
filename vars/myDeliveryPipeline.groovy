#!/usr/bin/groovy



def call(body) {

	println(body)

    // evaluate the body block, and collect configuration into the object

    def config = [:]

    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    //config = body;
    body()
	
        println(config)
	println 'config.BRANCH value :'
	println (config.BRANCH)
	
	println 'Map : ' 
	print body
	
	println 'config : '
	print config

	
println 'Start pipeline steps'
// our complete declarative pipeline can go in here
pipeline {
    agent { label 'Test_Node11' }
   // agent any
        stages {
           // stage checkout git
            stage('checkout git') {
		steps{ 
			sh "git branch:'master', url:'https://github.com/Sanjeev435/spring-petclinic.git'"
		    }
		}
          //checkout git ends
		    
          // stage build
           stage('build') {
		steps{
		    script{
			if(isUnix()){
                            sh 'mvn clean package -Dmaven.test.skip=true'
                        }
                        else{
                            bat('mvn clean install -Dmaven.test.skip=true')
                            }
		       }
                   }
               }
	   // stage build ends
		    
           // stage Test
           stage ('test') {
	      steps{
		   script{
			if('yes' == 'yes'){
				if(isUnix()){
				     parallel (
					"unit tests": { sh 'mvn test' },
					"integration tests": { sh 'mvn integration-test' }
				    )
				}
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
		 // stage Test Ends
	     }
            post {
                failure {
                    mail to: 'mrcool435@gmail.com', subject: 'Pipeline failed', body: "${env.BUILD_URL}"
                }
		success{
		   println "SUCCESS"
		}
            }
	}
}
