#!/bin/sh -xeu

cd app/

if [ -f target/zeebe-simple-monitor-app-*-SNAPSHOT.jar ]; then
    IMAGE="camunda/zeebe-simple-monitor:SNAPSHOT"

    echo "Building Zeebe Simple Monitor Docker image ${IMAGE}."
    docker build --no-cache -t ${IMAGE} .

    echo "Authenticating with DockerHub and pushing image."
    docker login --username ${DOCKER_HUB_USR} --password ${DOCKER_HUB_PSW}

    echo "Pushing ${IMAGE}"
    docker push ${IMAGE}
fi
