#!/bin/bash -xeu

cd target

export PROJECT_NAME="Zeebe Simple Monitor"
export GITHUB_TOKEN=${GITHUB_TOKEN_PSW}
export GITHUB_ORG=zeebe-io
export GITHUB_REPO=zeebe-simple-monitor
export ARTIFACT=zeebe-simple-monitor-${RELEASE_VERSION}.jar
export CHECKSUM=${ARTIFACT}.sha1sum

# create checksum files
sha1sum ${ARTIFACT} > ${CHECKSUM}

# do github release
curl -sL https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2 | tar xjvf - --strip 3

./github-release release --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --draft --name "${PROJECT_NAME} ${RELEASE_VERSION}" --description ""
./github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${ARTIFACT}" --file "${ARTIFACT}"
./github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"
