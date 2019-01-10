#!/usr/bin/groovy


pipeline{

       // agent { label 'Test_Node11' }
	   agent any
    
      stages{
              
         stage ('Deploy app release'){
                    //writeFile file: 'extras.json', text: "{'image_tag': '${IMAGE_TAG}', 'ecs_tasks': [${TASKS}]}"
                    //sh 'ansible-playbook site.yml -e "@extras.json"'
                    //new and working
                    steps{ 
                         sh 'ansible-playbook  ~/Documents/sampleAnsible/site.yml ' 
                       }
               }
              
         stage('Maven Install and clone Gitrepo'){
                   steps{
                      parallel (
                           "unit tests 1 ": { sh 'mvn clean install -Dmaven.test.failure.ignore -Dmaven.test.skip=true'},
						   "unit tests 2 ": { sh 'mvn clean install -Dmaven.test.failure.ignore -Dmaven.test.skip=true'},
						   "unit tests 3 ": { sh 'mvn clean install -Dmaven.test.failure.ignore -Dmaven.test.skip=true'},
						   "unit tests 4 ": { sh 'mvn clean install -Dmaven.test.failure.ignore -Dmaven.test.skip=true'}
                       )
                    }   
              }
        
         stage('Docker Build') {     
             steps {
			  parallel (
                     "Docker Build 1 ": { sh 'docker build -t sanjeev435/spring-petclinic:latest .'}, 
					 "Docker Build 2 ": { sh 'docker build -t sanjeev435/spring-petclinic:latest .'}
                   )
                }
           }
			
         stage('Docker Push') {
             steps {
                     withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_GLOBAL', passwordVariable: 'dockerHubPassword',
					 usernameVariable: 'dockerHubUser')]) {
                     sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                     sh 'docker push sanjeev435/spring-petclinic'}
			    }
			}
        }
		
	post {
          failure{
                  println "still facing"
              }
     
     success{
          println 'SUCCESS'
          mail to: 'shweta.idk@gmail.com',
             subject: "Successful Pipeline: ${currentBuild.fullDisplayName}",
             body: "Everything is correct with ${env.BUILD_URL}"
          }
     }
  }
