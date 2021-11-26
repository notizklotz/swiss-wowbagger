# Runtime container for non-native wowbagger
FROM europe-west6-docker.pkg.dev/swiss-wowbagger/docker/mbrola
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:11 $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"