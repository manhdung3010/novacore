pipeline {
    agent any

    parameters {
        string(
            name: 'ENV_FILE',
            defaultValue: '/opt/novacore/.env.prod',
            description: 'Absolute path to environment file'
        )
    }

    stages {

        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
                sh """
                    echo "Checking Docker installation..."
                    docker --version
                    docker compose version

                    echo "Using ENV file:"
                    echo ${params.ENV_FILE}

                    if [ ! -f "${params.ENV_FILE}" ]; then
                      echo "ENV file not found!"
                      exit 1
                    fi
                """
            }
        }

        stage('Deploy Infrastructure') {
            steps {
                sh """
                    set -e

                    docker compose \
                      --env-file ${params.ENV_FILE} \
                      -f docker/docker-compose.yml \
                      -f docker/docker-compose.prod.yml \
                      --profile infra \
                      up -d
                """
            }
        }

        stage('Build & Deploy App') {
            steps {
                sh """
                    set -e

                    docker compose \
                      --env-file ${params.ENV_FILE} \
                      -f docker/docker-compose.app.yml \
                      up -d --build
                """
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
        always {
            sh "docker ps"
        }
    }
}