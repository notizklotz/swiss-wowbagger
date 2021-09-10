gcloud config set project swiss-wowbagger && \
mvn clean package && \
gcloud run deploy swiss-wowbagger --image europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-jdk-server --region europe-west6