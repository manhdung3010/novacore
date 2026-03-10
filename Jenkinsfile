pipeline {
  agent any

  stages {

    stage('Build') {
      steps {
        sh 'mvn clean verify'
      }
    }

    stage('Deploy') {
      when {
        branch 'main'
      }
      steps {
        echo "Deploy production"
      }
    }

  }
}