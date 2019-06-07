#!/bin/bash -xeu

export GITHUB_TOKEN=${GITHUB_TOKEN_PSW}
export GITHUB_ORG=zeebe-io
export GITHUB_REPO=zeebe-simple-monitor

# do github release
curl -sL https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2 | tar xjvf - --strip 3
./github-release release --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --draft --name "Zeebe Hazelcast Exporter ${RELEASE_VERSION}" --description ""

# upload app
cd app/target

export ARTIFACT=zeebe-simple-monitor-app-${RELEASE_VERSION}.jar
export CHECKSUM=${ARTIFACT}.sha1sum

# create checksum files
sha1sum ${ARTIFACT} > ${CHECKSUM}

../../github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${ARTIFACT}" --file "${ARTIFACT}"
../../github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"

# upload exporter
cd ../../exporter/target

export ARTIFACT=zeebe-simple-monitor-exporter-${RELEASE_VERSION}.jar
export CHECKSUM=${ARTIFACT}.sha1sum

# create checksum files
sha1sum ${ARTIFACT} > ${CHECKSUM}

../../github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${ARTIFACT}" --file "${ARTIFACT}"
../../github-release upload --user ${GITHUB_ORG} --repo ${GITHUB_REPO} --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"
