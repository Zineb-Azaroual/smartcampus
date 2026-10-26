// Lance Maven avec sh sur un agent Linux et bat sur un agent Windows
def runMaven(String args) {
    if (isUnix()) {
        sh "mvn ${args}"
    } else {
        bat "mvn ${args}"
    }
}

pipeline {
    agent any

    options {
        // Le checkout est fait explicitement dans le stage Checkout
        skipDefaultCheckout()
    }

    tools {
        maven 'maven'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                runMaven '-B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                runMaven '-B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
