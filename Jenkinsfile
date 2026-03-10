pipeline {
    agent any

    parameters {
        string(
            name: 'ENV_FILE',
            defaultValue: '.env',
            description: 'Path to env file'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Deploy Infrastructure') {
            steps {
                sh '''
                    docker compose \
                      --env-file ${ENV_FILE} \
                      -f docker/docker-compose.yml \
                      -f docker/docker-compose.prod.yml \
                      --profile infra \
                      up -d
                '''
            }
        }

        stage('Build & Deploy App') {
            steps {
                sh '''
                    docker compose \
                      --env-file ${ENV_FILE} \
                      -f docker/docker-compose.app.yml \
                      up -d --build
                '''
            }
        }

    }

    post {
        success {
            echo "Deploy success"
        }
        failure {
            echo "Deploy failed"
        }
    }
}