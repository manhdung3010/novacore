pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

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
                git branch: 'main',
                    url: 'https://github.com/manhdung3010/novacore.git'
            }
        }

        stage('Verify Environment') {
            steps {
                sh '''
                    docker --version
                    docker compose version

                    if [ ! -f "$ENV_FILE" ]; then
                        echo "ENV file not found!"
                        exit 1
                    fi
                '''
            }
        }

        stage('Deploy Infrastructure') {
            steps {
                sh '''
                    docker compose \
                      --env-file $ENV_FILE \
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
                      --env-file $ENV_FILE \
                      -f docker/docker-compose.app.yml \
                      up -d --build
                '''
            }
        }

    }

    post {
        always {
            sh "docker ps"
        }
    }
}