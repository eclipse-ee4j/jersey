#!/usr/bin/env groovy

/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/**
 * Release script for Jakarta EE8/9 Jersey versions
 * Release branches are 2.x or 3.0
 *
 * Input Parameters:
 * RELEASE_VERSION - type: String
 *                 - example:  2.42
 *                 - Description: mandatory release version. Later is being published to staging/central.
 * NEXT_VERSION    - type: String
 *                 - example:  2.43-SNAPSHOT
 *                 - Description: mandatory next developing version will be committed in to the release branch and later
 *                               merged into the original release branch for further developing.
 * BRANCH          - type: Choice Parameter
 *                 - values: 2.x
 *                           3.0
 *                 - Description: Original release branch from which the codebase is being checked out
 * DRY_RUN         - type: Boolean
 *                 - Description: if checked nothing is really committed to Git nor published to the staging
 * OVERWRITE       - type: Boolean
 *                 - Description: if checked allows replacing of the previously published RELEASE_VERSION of Jersey
 *
 */

node {

    def MVN_HOME = tool name: 'apache-maven-latest', type: 'maven'
    def HELP_PLUGIN = 'org.apache.maven.plugins:maven-help-plugin:2.1.1'
    def TARGET = 'package javadoc:jar gpg:sign install:install'
    def DEPLOY_TARGET = ''
    def RELEASE_VERSION = RELEASE_VERSION
    def NEXT_VERSION = NEXT_VERSION
    def RELEASE_TAG = ''
    def NEW_RELEASE_BRANCH = ''
    def OVERWRITE = OVERWRITE
    def PROJECT_NAME = 'Jersey'
    def LOGIN = 'jersey-bot'
    def EMAIL = 'jersey-bot@eclipse.org'
    def REPO = 'git@github.com:eclipse-ee4j/jersey.git'
    def SECRET_FILE_ID = 'secret-subkeys.asc'
    def CREDENTIALS_ID = 'github-bot-ssh'
    def GIT_ORIGIN = 'origin'
    def RELEASE_FOLDER = env.WORKSPACE
    def RELEASE_BRANCH = BRANCH

    def STAGING_NAME_PATTERN = 'orgglassfishjersey-[0-9]+'
    def STAGING_NAME = ''
    def STAGING_PREV_NAME = ''
    def STAGING_OPEN_NAME = ''
    def STAGING_DESC = 'org.glassfish.jersey:' + RELEASE_VERSION
    def STAGING_BOM_DESC = 'org.glassfish.jersey.bom:' + RELEASE_VERSION
    def STAGING_PROFILE_ID = '70fa3a107a8918'

    def JDK_11_HOME = tool name: 'openjdk-jdk11-latest', type: 'jdk'
    def JDK_12_HOME = tool name: 'openjdk-jdk12-latest', type: 'jdk'
    def JDK_17_HOME = tool name: 'openjdk-jdk17-latest', type: 'jdk'
    def JDK_8_HOME = tool name: 'oracle-jdk8-latest', type: 'jdk'

    def ZX_BRANCH_MODULES = ',:jersey-jetty-connector,:jersey-container-jetty-http,:jersey-container-jetty-servlet,:jersey-test-framework-provider-jetty,:jersey-test-framework-core,:jersey-container-servlet-core'

    env.JAVA_HOME = JDK_8_HOME
    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}:${MVN_HOME}/bin"

    if (RELEASE_BRANCH == '2.x') {
        ZX_BRANCH_MODULES = ''
    }

    sh 'java -version'

    stage('Fetch from git') {
        git(branch: RELEASE_BRANCH, credentialsId: CREDENTIALS_ID, url: REPO)
    }
    stage('Prepare environment') {

        dir(RELEASE_FOLDER) {
            //# Check whether top level pom.xml contains SNAPSHOT version
            if (!sh(returnStdout: true, script: "grep '<version>' pom.xml | grep 'SNAPSHOT'")?.trim()) {
                error('-[ Missing SNAPSHOT version in POM! ]-------------------------------------------')
            }

            //# Compute release versions
            def SNAPSHOT_VERSION = sh(returnStdout: true, script: 'mvn -q -Dexec.executable="echo" -Dexec.args=\'${project.version}\' --non-recursive exec:exec -Pstaging').trim()

            if (!RELEASE_VERSION?.trim()) {
                if (!SNAPSHOT_VERSION?.trim()) {
                    error('-[ Missing required snapshot version number! ]----------------------------------')
                } else {
                    def versionTokens = SNAPSHOT_VERSION.split('-')
                    RELEASE_VERSION = versionTokens[0]
                }
            }

            if (!NEXT_VERSION?.trim()) {
                def (MAJOR_VERSION, MINOR_VERSION) = RELEASE_VERSION.tokenize('.')
                def NEXT_MINOR_VERSION = (MINOR_VERSION as Integer) + 1
                NEXT_VERSION = MAJOR_VERSION + '.' + NEXT_MINOR_VERSION + '-SNAPSHOT'
            }

            RELEASE_TAG = RELEASE_VERSION
            NEW_RELEASE_BRANCH = RELEASE_VERSION+'-BRANCH'

            echo "Current version: ${SNAPSHOT_VERSION}"
            echo "Release version: ${RELEASE_VERSION}"
            echo "Next version:    ${NEXT_VERSION}"
            echo "Release tag:     ${RELEASE_TAG}"
            echo "Release branch:  ${NEW_RELEASE_BRANCH}"

            if (!SNAPSHOT_VERSION?.trim() || !RELEASE_VERSION?.trim() || !NEXT_VERSION?.trim()) {
                error '-[ Missing required version numbers! ]------------------------------------------'
            }

            if (DRY_RUN == 'true') {
                echo '-[ Dry run turned on ]----------------------------------------------------------'
                //TARGET = 'install'
            } else {
                DEPLOY_TARGET = ' deploy:deploy'
            }
            echo '-[ Configure git user ]--------------------------------------------------------'
            sh "git config --local user.email \"${EMAIL}\""
            sh "git config --local user.name \"$LOGIN\""

        }

    }
    stage('Prepare GPG') {
        withCredentials([file(credentialsId: SECRET_FILE_ID, variable: 'KEYRING')]) {
            //# Workaround: GPG initialization
            sh("gpg --batch --import ${KEYRING}")
            sh '''
                for fpr in $(gpg --list-keys --with-colons  | awk -F: '/fpr:/ {print $10}' | sort -u);
                do
                    echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key $fpr trust;
                done
                '''
        }
    }
    stage('Prepare branch') {
        echo '-[ Prepare branch ]-------------------------------------------------------------'

        echo '-[ Switching to release branch ]-------------------------------------------------'
        sh """
        git checkout ${GIT_ORIGIN}/${RELEASE_BRANCH} && true
        git reset --hard ${GIT_ORIGIN}/${RELEASE_BRANCH} && true
        git checkout -B ${NEW_RELEASE_BRANCH}
        """
        echo '-[ Release tag cleanup ]--------------------------------------------------------'
        def TAG_NAME = sh(returnStdout: true, script: "git tag | grep ${RELEASE_TAG} || true").trim()
        if (RELEASE_TAG == TAG_NAME) {
            if (OVERWRITE == 'true') {
                echo "${RELEASE_TAG} tag already exists, deleting"
                sshagent([CREDENTIALS_ID]) {
                    sh "git push --delete origin ${RELEASE_TAG} && true"
                }
            } else {
                error "${RELEASE_TAG} tag already exists"
            }
            //# Always delete local tag if exists
            sh """
            git tag --delete ${RELEASE_TAG} && true
            """
        }
        sh '''
        sed -i 's|<suppress files="generated" checks=".*"/>|<suppress files="generated" checks=".*"/><suppress files="unpacked-src" checks=".*"/>|g' etc/config/checkstyle-suppressions.xml
        '''
    }
    stage('Check previous stagings') {
        if (DRY_RUN == 'true') {
            echo DRY_RUN + ' ------'
        } else {
            if (OVERWRITE == 'true') {
                STAGING_PREV_NAME = sh(returnStdout: true,
                        script: "mvn -B --non-recursive -Pstaging nexus-staging:rc-list | awk '/\\[INFO] $STAGING_NAME_PATTERN[ ]+CLOSED[ ]+$STAGING_DESC[ ]*\$/ {if(a){a = \$2\",\"a} else{a = \$2}}END{print a}'").trim()
                echo 'Previously closed staging name: ' + STAGING_PREV_NAME
            }

            STAGING_OPEN_NAME = sh(returnStdout: true,
                    script: "mvn -B --non-recursive -Pstaging nexus-staging:rc-list | awk  '/$STAGING_NAME_PATTERN OPEN / {if(a){a = \$2\",\"a} else{a = \$2}}END{print a}'").trim()

            if (!STAGING_OPEN_NAME?.trim()) {
                echo 'No currently open stagings'
            } else {
                echo 'Currently open redundand staging: ' + STAGING_OPEN_NAME + ', immediately closing'
                sh """
                    OPEN_STAGINGS=${STAGING_OPEN_NAME}
                    mvn -B -q -Pstaging nexus-staging:rc-drop -DstagingRepositoryId=\${OPEN_STAGINGS}  
                """
            }
        }
    }
    /*
    stage('Open new staging') {
        if (DRY_RUN == 'true') {
            echo DRY_RUN + ' ------'
        } else {
            STAGING_NAME = sh(returnStdout: true,
                script: "mvn -B --non-recursive -Pstaging -DstagingProfileId=${STAGING_PROFILE_ID} -DstagingDescription=${STAGING_DESC} nexus-staging:rc-open | awk  '/\\[INFO] Opened / {print \$3}'").trim()
            echo 'New staging name: '+STAGING_NAME
        }
    }*/
    stage("Build ${PROJECT_NAME}") {
        echo env.JAVA_HOME
        echo '-[ Run maven release ]---------------------------------------------------------'
        echo '-[ Set Release version ]-------------------------------------------------------'
        sh """
        cd ${RELEASE_FOLDER}
        mvn -B -V -Pstaging versions:set -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false
        cd bom
        mvn -B -V -Pstaging versions:set -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false
        cd ..
        """
        echo '-[ Update Copyright years ]----------------------------------------------------'
        sh '''#!/bin/bash -e
        
            export CURRENT_YEAR=`date '+%Y'`
            export SED_CMD_LINE='sed -i "s#, 20.. Oracle and/or its affiliates#, ${CURRENT_YEAR} Oracle and/or its affiliates#g"'

            git status --porcelain --untracked-files=no > modified_pom.log
            cp modified_pom.log list_of_poms.txt
            sed -i "s| M |$SED_CMD_LINE |g" modified_pom.log
            sed -i "s| M ||g" list_of_poms.txt

            bash modified_pom.log

            echo ${CURRENT_YEAR} current year

            while IFS= read -r path_to_pom
            do
              export CP_YEAR=`grep -o 'Copyright (c) 20.. Oracle and/or its affiliates' ${path_to_pom} | awk '{print $3}'`
              [[ -z ${CP_YEAR} ]] && CP_YEAR=${CURRENT_YEAR} || echo ${CP_YEAR}
              [[ ${CP_YEAR} == ${CURRENT_YEAR} ]] || ( sed -i "s#Copyright (c) ${CP_YEAR} Oracle and/or its affiliates#Copyright (c) ${CP_YEAR}, ${CURRENT_YEAR} Oracle and/or its affiliates#g" ${path_to_pom} ; echo ${path_to_pom} )
              unset CP_YEAR
            done < "list_of_poms.txt"
        '''
        echo '-[ Run release build ]---------------------------------------------------------'
        dir(RELEASE_FOLDER) {
            env.JAVA_HOME = JDK_12_HOME
            //
            sh "mvn -am -Pstaging --projects  core-server,core-client,media/jaxb,inject/hk2,ext/wadl-doclet,core-common${ZX_BRANCH_MODULES} clean install -B -q -V -DskipTests"
            if (RELEASE_BRANCH == '3.0') {
                env.JAVA_HOME = JDK_17_HOME
                sh "mvn clean install -B -q -V -DskipTests -am -Pstaging --projects :jersey-spring6,connectors/helidon-connector"
            }
            env.JAVA_HOME = JDK_8_HOME
            sh "mvn -q -B -V -DskipTests -Ddoclint=none -Dadditionalparam='-Xdoclint:none' -Dcheckstyle.skip " +
                    //" -DstagingDescription='${STAGING_DESC}' -DstagingRepositoryId='${STAGING_NAME}' "+
                    " -Poss-release,staging -U -C ${TARGET} ${DEPLOY_TARGET}"

            sh "mvn -Poss-release,staging --projects core-server,core-client,media/jaxb,inject/hk2,ext/wadl-doclet,core-common install gpg:sign ${DEPLOY_TARGET} -B -q -V -DskipTests"
            if (RELEASE_BRANCH == '2.x') {
                env.JAVA_HOME = JDK_11_HOME
                sh "mvn -Poss-release,staging --projects connectors/helidon-connector install gpg:sign ${DEPLOY_TARGET} -B -q -V -DskipTests"
            }
        }
    }
    stage('Prepare release') {
        sh '''
        git checkout -- etc/config/checkstyle-suppressions.xml
        '''
        echo '-[ Perform release commit to git ]---------------------------------------------'
        sh "git commit -a -m ${RELEASE_VERSION}"
        sh "git tag -m ${RELEASE_TAG} -a ${RELEASE_TAG}"
        echo '-[ Set next snapshot version ]-------------------------------------------------'
        dir(RELEASE_FOLDER) {
            sh """
                mvn -B -Pstaging versions:set -DnewVersion=${NEXT_VERSION} -DgenerateBackupPoms=false 
                cd bom
                mvn -B -Pstaging versions:set -DnewVersion=${NEXT_VERSION} -DgenerateBackupPoms=false
                cd ..
            """
        }
        echo '-[ Perform commit to git ]-----------------------------------------------------'
        sh "git commit -a -m ${NEXT_VERSION}"
    }
    stage('Publish release') {
        if (DRY_RUN == 'true') {
            echo '-[ Prepared branch ]----------------------------------------------------------'
            sh "git branch --list ${NEW_RELEASE_BRANCH}"
            echo '-[ Prepared tag ]-------------------------------------------------------------'
            sh "git tag --list ${NEW_RELEASE_BRANCH}"
            echo '-[ Prepared commits ]---------------------------------------------------------'
            sh 'git log -n 5'
            sshagent([CREDENTIALS_ID]) {
                sh "git push ${GIT_ORIGIN} ${NEW_RELEASE_BRANCH} --dry-run"
            }
            return
        } else {
            sshagent([CREDENTIALS_ID]) {
                sh "git push -f ${GIT_ORIGIN} ${NEW_RELEASE_BRANCH} --follow-tags"
            }
        }
    }
    stage('Find related staging') {
        if (DRY_RUN != 'true') {
            if (!STAGING_NAME?.trim()) {
                try {
                    STAGING_NAME =
                            sh(returnStdout: true,
                                    script: "mvn -B --non-recursive -Pstaging nexus-staging:rc-list | awk  '/$STAGING_NAME_PATTERN OPEN / {if(a){a = \$2\",\"a} else{a = \$2}}END{print a}'"
                            ).trim()
                } catch (Error err) {
                    currentBuild.result = 'UNSTABLE'
                }
            }
            echo 'Staging name: ' + STAGING_NAME
        } else {
            echo 'DRY_RUN'
        }
    }
    stage('Close released staging') {
        if (!STAGING_NAME?.trim()) {
            echo 'Nothing to be closed'
        } else {
            sh("mvn -B -q -Pstaging nexus-staging:rc-close -DstagingRepositoryId=${STAGING_NAME} -DstagingDescription='${STAGING_DESC}'")
        }
    }
    stage('Drop redundant staging') {
        if (!STAGING_PREV_NAME?.trim()) {
            echo 'Nothing to be dropped'
        } else {
            try {
                sh("""
                OPEN_STAGINGS=${STAGING_PREV_NAME}
                mvn -B -q -Pstaging nexus-staging:rc-drop -DstagingRepositoryId=\${OPEN_STAGINGS}
                """)
            } catch (Error err) {
                currentBuild.result = 'UNSTABLE'
            }
        }
    }
}