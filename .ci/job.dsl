// vim: set filetype=groovy:

def jobName = 'zeebe-simple-monitor-DISTRO-maven-deploy'
def repository = 'zeebe-simple-monitor'
def gitBranch = 'master'

def pom = 'pom.xml'
def mvnGoals = 'clean verify -B'

def mavenVersion = 'maven-3.3-latest'
def mavenSettingsId = 'camunda-maven-settings'

// script to set access rights on ssh keys
// and configure git user name and email
def setupGitConfig = '''\
#!/bin/bash -xe

chmod 600 ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa.pub

git config --global user.email "ci@camunda.com"
git config --global user.name "camunda-jenkins"
'''

def mavenGpgKeys = '''\
#!/bin/bash

if [ -e "${MVN_CENTRAL_GPG_KEY_SEC}" ]
then
  gpg -q --allow-secret-key-import --import ${MVN_CENTRAL_GPG_KEY_SEC} || echo 'Private GPG Sign Key is already imported!.'
  rm ${MVN_CENTRAL_GPG_KEY_SEC}
else
  echo 'Private GPG Key not found.'
fi

if [ -e "${MVN_CENTRAL_GPG_KEY_PUB}" ]
then
  gpg -q --import ${MVN_CENTRAL_GPG_KEY_PUB} || echo 'Public GPG Sign Key is already imported!.'
  rm ${MVN_CENTRAL_GPG_KEY_PUB}
else
  echo 'Public GPG Key not found.'
fi
'''

def githubRelease = '''\
#!/bin/bash

# do github release
curl -sL https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2 | tar xjvf - --strip 3

./github-release release --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "Zeebe Simple Monitor ${RELEASE_VERSION}" --description ""


cd app/target

JAR="zeebe-simple-monitor-app-${RELEASE_VERSION}.jar"
CHECKSUM="${JAR}.sha1sum"

# create checksum files
sha1sum ${JAR} > ${CHECKSUM}

../../github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${JAR}" --file "${JAR}"
../../github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"

cd ../../exporter/target

JAR="zeebe-simple-monitor-exporter-${RELEASE_VERSION}.jar"
CHECKSUM="${JAR}.sha1sum"

# create checksum files
sha1sum ${JAR} > ${CHECKSUM}

../../github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${JAR}" --file "${JAR}"
../../github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"

'''

def dockerRelease = '''\
#!/bin/bash -xeu

cd app/

# clear docker host env set by jenkins job
unset DOCKER_HOST

IMAGE="camunda/zeebe-simple-monitor"

echo "Building Zeebe Simple Monitor Docker image ${RELEASE_VERSION}."
docker build --no-cache --build-arg JAR=target/zeebe-simple-monitor-app-${RELEASE_VERSION}.jar -t ${IMAGE}:${RELEASE_VERSION} .

echo "Authenticating with DockerHub and pushing image."
docker login --username ${DOCKER_HUB_USERNAME} --password ${DOCKER_HUB_PASSWORD} --email ci@camunda.com

echo "Pushing ${IMAGE}:${RELEASE_VERSION}"
docker push ${IMAGE}:${RELEASE_VERSION}

docker tag -f ${IMAGE}:${RELEASE_VERSION} ${IMAGE}:latest

echo "Pushing ${IMAGE}:latest"
docker push ${IMAGE}:latest
'''

def dockerSnapshot = '''\
#!/bin/bash
cd app/
# clear docker host env set by jenkins job
unset DOCKER_HOST

if [ -f target/zeebe-simple-monitor-app-*-SNAPSHOT.jar ]; then
    IMAGE="camunda/zeebe-simple-monitor:SNAPSHOT"

    echo "Building Zeebe Simple Monitor Docker image ${IMAGE}."
    docker build --no-cache -t ${IMAGE} .

    echo "Authenticating with DockerHub and pushing image."
    docker login --username ${DOCKER_HUB_USERNAME} --password ${DOCKER_HUB_PASSWORD} --email ci@camunda.com

    echo "Pushing ${IMAGE}"
    docker push ${IMAGE}
fi
'''

// properties used by the release build
def releaseProperties = [
    resume: 'false',
    tag: '${RELEASE_VERSION}',
    releaseVersion: '${RELEASE_VERSION}',
    developmentVersion: '${DEVELOPMENT_VERSION}',
    pushChanges: '${PUSH_CHANGES}',
    remoteTagging: '${PUSH_CHANGES}',
    localCheckout: '${USE_LOCAL_CHECKOUT}',
    arguments: '--settings=${NEXUS_SETTINGS} -DskipTests=true -Dgpg.passphrase="${GPG_PASSPHRASE}" -Dskip.central.release=${SKIP_DEPLOY_TO_MAVEN_CENTRAL} -Dskip.camunda.release=${SKIP_DEPLOY_TO_CAMUNDA_NEXUS}',
]


mavenJob(jobName)
{
    scm
    {
        git
        {
            remote
            {
                github 'zeebe-io/' + repository, 'ssh'
                credentials 'camunda-jenkins-github-ssh'
            }
            branch gitBranch
            extensions
            {
                localBranch gitBranch
            }
        }
    }
    triggers
    {
        githubPush()
    }
    label 'dind'
    jdk 'jdk-8-latest'

    rootPOM pom
    goals mvnGoals
    localRepository LocalRepositoryLocation.LOCAL_TO_WORKSPACE
    providedSettings mavenSettingsId
    mavenInstallation mavenVersion

    postBuildSteps('SUCCESS') {
        shell dockerSnapshot
    }

    wrappers
    {
        timestamps()

        timeout
        {
            absolute 60
        }

        configFiles
        {
            // jenkins github public ssh key needed to push to github
            custom('Jenkins CI GitHub SSH Public Key')
            {
                targetLocation '/home/camunda/.ssh/id_rsa.pub'
            }
            // jenkins github private ssh key needed to push to github
            custom('Jenkins CI GitHub SSH Private Key')
            {
                targetLocation '/home/camunda/.ssh/id_rsa'
            }
            // nexus settings xml
            mavenSettings(mavenSettingsId)
            {
                variable('NEXUS_SETTINGS')
            }
        }

        credentialsBinding {
	  // maven central signing credentials
          string('GPG_PASSPHRASE', 'password_maven_central_gpg_signing_key')
          file('MVN_CENTRAL_GPG_KEY_SEC', 'maven_central_gpg_signing_key')
          file('MVN_CENTRAL_GPG_KEY_PUB', 'maven_central_gpg_signing_key_pub')
          // github token for release upload
          string('GITHUB_TOKEN', 'github-camunda-jenkins-token')
          // docker hub credentials for docker push
          usernamePassword('DOCKER_HUB_USERNAME', 'DOCKER_HUB_PASSWORD', 'camundajenkins-dockerhub')
        }

        release
        {
            doNotKeepLog false
            overrideBuildParameters true

            parameters
            {
                stringParam('RELEASE_VERSION', '0.1.0', 'Version to release')
                stringParam('DEVELOPMENT_VERSION', '0.2.0-SNAPSHOT', 'Next development version')
                booleanParam('PUSH_CHANGES', true, 'If <strong>TRUE</strong>, push the changes to remote repositories.  If <strong>FALSE</strong>, do not push changes to remote repositories. Must be used in conjunction with USE_LOCAL_CHECKOUT = <strong>TRUE</strong> to test the release!')
                booleanParam('USE_LOCAL_CHECKOUT', false, 'If <strong>TRUE</strong>, uses the local git repository to checkout the release tag to build.  If <strong>FALSE</strong>, checks out the release tag from the remote repositoriy. Must be used in conjunction with PUSH_CHANGES = <strong>FALSE</strong> to test the release!')
		booleanParam('SKIP_DEPLOY_TO_MAVEN_CENTRAL', false, 'If <strong>TRUE</strong>, skip the deployment to maven central. Should be used when testing the release.')
                booleanParam('SKIP_DEPLOY_TO_CAMUNDA_NEXUS', false, 'If <strong>TRUE</strong>, skip the deployment to camunda nexus. Should be used when testing the release.')
            }

            preBuildSteps
            {
                // setup git configuration to push to github
                shell setupGitConfig

                // execute maven release
                maven
                {
                    mavenInstallation mavenVersion
                    providedSettings mavenSettingsId
                    goals 'release:prepare release:perform -Dgpg.passphrase="${GPG_PASSPHRASE}" -B'

                    properties releaseProperties
                    localRepository LocalRepositoryLocation.LOCAL_TO_WORKSPACE
                }

                shell githubRelease

                shell dockerRelease

            }

        }

    }

    publishers
    {

        archiveJunit('**/target/surefire-reports/*.xml')
        {
            retainLongStdout()
        }

        extendedEmail
        {
          triggers
          {
              firstFailure
              {
                  sendTo
                  {
                      culprits()
                  }
              }
              fixed
              {
                  sendTo
                  {
                      culprits()
                  }
              }
          }
        }
    }

    logRotator(-1, 5, -1, 1)

}
