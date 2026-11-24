pipeline {
    agent any
    
    environment {
        PATH = "C:\\Windows\\System32;${env.PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                bat 'echo "Simulation du Build reussie" > artefact.txt'
            }
        }
        stage('Test') {
            steps {
                bat 'echo "Simulation des Tests reussie"'
            }
        }
    }
    
    post {
        success {
            archiveArtifacts artifacts: 'artefact.txt', fingerprint: true
        }
    }
}