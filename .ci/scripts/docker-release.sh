#!/bin/sh -xeu

cd app/

IMAGE="camunda/zeebe-simple-monitor"

echo "Building Zeebe Simple Monitor Docker image ${RELEASE_VERSION}."
docker build --no-cache --build-arg JAR=target/zeebe-simple-monitor-app-${RELEASE_VERSION}.jar -t ${IMAGE}:${RELEASE_VERSION} .

echo "Authenticating with DockerHub and pushing image."
docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}

echo "Pushing ${IMAGE}:${RELEASE_VERSION}"
docker push ${IMAGE}:${RELEASE_VERSION}

docker tag ${IMAGE}:${RELEASE_VERSION} ${IMAGE}:latest

echo "Pushing ${IMAGE}:latest"
docker push ${IMAGE}:latest
