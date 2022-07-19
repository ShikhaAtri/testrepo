def call(String repourl,String credid,String sonarname,String sonarid,String branch) {
def choice=[]
pipeline {
    agent any
    tools {
    	maven 'My_Maven'
	}
    stages {
        stage('Git Checkout') {
            steps {
		    git branch: "${branch}", credentialsId: "${credid}", url: "${repourl}"
            }
        }    
        stage('Compile') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage("Sonarcloud analysis") {
            steps {
		    withSonarQubeEnv(credentialsId: "${sonarid}", installationName: "${sonarname}") {
                    sh 'mvn verify sonar:sonar -Dsonar.organization=atri -Dsonar.projectKey=sonar-pp1  -Dsonar.sources=src/main/ -Dsonar.tests=src/test/ -Dsonar.exclusions=src/main/java/** -Dsonar.host.url=https://sonarcloud.io/'
                    sh 'sleep 60'
                }
            }
        }
        stage("Quality Gate") {
            steps {
              timeout(time: 1, unit: 'HOURS') {
                waitForQualityGate abortPipeline: true
              }
            }
          }
          stage('Builds_backup') {
              steps {
		      sh 'sudo mkdir -p /opt/backup'
		      sh 'sudo mv target/my-app-1.0-SNAPSHOT.jar target/1.0.${BUILD_NUMBER}.jar'
		      sh 'sudo cp target/1.0.${BUILD_NUMBER}.jar /opt/backup/'
              }
          }
       } 
    }
}
