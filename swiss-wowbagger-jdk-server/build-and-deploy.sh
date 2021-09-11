gcloud config set project swiss-wowbagger && \
docker build . -t europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-jdk-server && \
docker push europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-jdk-server && \
gcloud run deploy swiss-wowbagger --image europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-jdk-server --region europe-west6