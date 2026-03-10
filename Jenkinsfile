pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                echo 'Jenkins pipeline is running'

                sh '''
                    echo "Current user:"
                    whoami

                    echo "Current directory:"
                    pwd

                    echo "List files:"
                    ls -la
                '''
            }
        }
    }
}