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

def githubRelease = '''\
#!/bin/bash

cd target

JAR="zeebe-simple-monitor-${RELEASE_VERSION}.jar"
CHECKSUM="${JAR}.sha1sum"

# create checksum files
sha1sum ${JAR} > ${CHECKSUM}

# do github release
curl -sL https://github.com/aktau/github-release/releases/download/v0.7.2/linux-amd64-github-release.tar.bz2 | tar xjvf - --strip 3

./github-release release --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "Zeebe Simple Monitor ${RELEASE_VERSION}" --description ""
./github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${JAR}" --file "${JAR}"
./github-release upload --user zeebe-io --repo zeebe-simple-monitor --tag ${RELEASE_VERSION} --name "${CHECKSUM}" --file "${CHECKSUM}"
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
    arguments: '--settings=${NEXUS_SETTINGS} -DskipTests=true -Dskip.central.release=true -Dskip.camunda.release=true',
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
    label 'ubuntu'
    jdk 'jdk-8-latest'

    rootPOM pom
    goals mvnGoals
    localRepository LocalRepositoryLocation.LOCAL_TO_WORKSPACE
    providedSettings mavenSettingsId
    mavenInstallation mavenVersion

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
          // github token for release upload
          string('GITHUB_TOKEN', 'github-camunda-jenkins-token')
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
                    goals 'release:prepare release:perform -B'

                    properties releaseProperties
                    localRepository LocalRepositoryLocation.LOCAL_TO_WORKSPACE
                }

                shell githubRelease

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
