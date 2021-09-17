gcloud config set project swiss-wowbagger && \
mvn clean package && \
gcloud run deploy swiss-wowbagger --image europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-jdk-server --port=7125 --region europe-west6 --max-instances=1 --memory=128Mi --ingress=all --allow-unauthenticated && \
gcloud run deploy swiss-wowbagger-telegram --image europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-telegram --port=80 --region europe-west6 --max-instances=1 --memory=128Mi --ingress=all --allow-unauthenticated && \
gcloud run deploy swiss-wowbagger-twitter --image europe-west6-docker.pkg.dev/swiss-wowbagger/wowbagger-docker-repo/swiss-wowbagger-twitter --region europe-west6 --max-instances=1 --memory=128Mi --ingress=all --allow-unauthenticated