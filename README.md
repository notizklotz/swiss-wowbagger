# swiss-wowbagger
Let yourself be insulted in swiss german.

Inspired by kthxbye's [figgdi](http://figgdi.kthxbye.ch/).
Thanks to Franz Hohler's [Totemügerli](https://www.youtube.com/watch?v=DQi0lsUs8J4),
Stiller Has' [grusig](https://www.youtube.com/watch?v=dfL_IRXVLtQ).

**[Huere Siech, säg mou öppis!](https://nidi3.github.io/swiss-wowbagger)**

## GCloud Project

The apps and the docker containers are hosted at Google Cloud

https://console.cloud.google.com/run?project=swiss-wowbagger

[Schaltstelle](https://www.schaltstelle.ch) Members can add themselves as _Owner_ of the project.

## Register Telegram Bot Webhook
`curl -F "url=https://swiss-wowbagger-telegram-ultgi7by3q-oa.a.run.app/${WOWBAGGER_BOT_TOKEN}/webhook" https://api.telegram.org/bot${WOWBAGGER_BOT_TOKEN}/setWebhook`

For local testing:
1. create a public tunnel to localhost using `ngrok http 8080`
2. Switch the Telegram Bot registration to your public ngrok HTTPS URL using the curl command above

## Twitter Bot development


"Read and write and Direct message" in App permissions before creating the acccess tokens

Create Environment
https://developer.twitter.com/en/account/environments

1. Run `TwitterBot` using the various ENV variables
2. Run `ngrok http 8080`
3. Run `twurl -d 'url=https://a9da-2a02-168-a9fe-0-73ba-8f2b-996a-a99f.ngrok.io/webhook' /1.1/account_activity/all/test/webhooks.json`. Replacing the ngrok url with your proper one
4. 

## Setup Google Cloud SDK access
Only needed if you need access to the project's Docker repo or if you want to deploy from localhost.

1. Install Google Cloud SDK https://cloud.google.com/sdk/docs/install
2. Setup Google Cloud SDK https://cloud.google.com/sdk/docs/initializing (Project ID: swiss-wowbagger)
3. Configure authentication for the docker repo: `gcloud auth configure-docker europe-west6-docker.pkg.dev`

## Update Docker base image

The base image is referenced using digest instead of tags to prevent working with out-of-date images.

1. Setup Google Cloud access (see section above)
2. `cd swiss-wowbagger-docker-mbrola && ./build.sh`
3. Update swiss-wowbagger-docker-mbrola container digest in the parent `pom.xml`

## Locally run Docker images

Please note that [Jib](https://github.com/GoogleContainerTools/jib) is used for building and pushing the Docker
images. Per default it doesn't use the Docker daemon so the images are not available locally. See
https://github.com/GoogleContainerTools/jib/blob/master/docs/faq.md#can-i-build-to-a-local-docker-daemon for options or
pull the images from the GCloud repo.