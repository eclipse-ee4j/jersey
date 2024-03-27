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
 * Runs TCK for Jersey EE8 on GlassFish 5
 *
 * Input Parameters:
 *
 *   GF_INTEGRATION_JOB_NAME    - type: String
 *                              - value: Jersey_Staging_GF_Integration
 *                              - description: name of a job from which the prepared archive of the Glassfish 5 is taken
 *                              - possible values: Jersey_binaries_GF_integration
 *                                                 Jersey_Staging_GF_Integration
 *                                                 GF5_Jersey2_Archive_Integration
 *   TS_JTE_JOB_NAME            - type: String
 *                              - value: ts_jte_alter
 *                              - description: name of a job from which prepared ts.jte is taken
 *  TCK_BUNDLE_JOB_OR_URL       - type: String
 *                              - value: tck_build
 *                              - description: name of a job from which prepared TCK bundle is taken
 */

node {

    // Job that created the API artifact
    def GF_BUILD_JOB = "${env.GF_INTEGRATION_JOB_NAME}"
    def TS_JTE_BUILD_JOB = "${env.TS_JTE_JOB_NAME}"
    def TCK_BUNDLE_URL = "${env.TCK_BUNDLE_JOB_OR_URL}"
    def API_JAR_NAME="jakarta.ws.rs-api.jar"

    echo "GF_BUILD_JOB=${GF_BUILD_JOB}"
    echo "TS_JTE_BUILD_JOB=${TS_JTE_BUILD_JOB}"
    echo "TCK_BUNDLE_URL=${TCK_BUNDLE_URL}"

    //TCK properties
    env.deliverabledir="jaxrs"
    env.tck_root="restful-ws-tck"
    env.TS_HOME="${env.WORKSPACE}/${env.tck_root}"
    env.javaee_home="${env.WORKSPACE}/glassfish5"

    env.JAVA_HOME= tool name: 'oracle-jdk8-latest', type: 'jdk'
    env.ANT_HOME= tool name: 'apache-ant-latest', type: 'ant'
    env.PATH="${ANT_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
    env.ANT_OPTS="-Djavax.xml.accessExternalSchema=all"

    stage("Grab GF and ts.jte artifacts") {
        //https://go.cloudbees.com/docs/cloudbees-documentation/cjoc-user-guide/index.html#cluster-copy-artifacts
        dir ("download") {
            copyArtifacts(projectName: "${GF_BUILD_JOB}")
            copyArtifacts(projectName: "${TS_JTE_BUILD_JOB}")
        }
    }

    stage("Grab TCK bundle") {
        env.TCK_BUNDLE_URL = "${TCK_BUNDLE_URL}"
        if (!(env.TCK_BUNDLE_URL).startsWith("http")) {
            dir ("download") {
                copyArtifacts(projectName: "${TCK_BUNDLE_URL}", filter: "**/*_latest.zip")
                //flatten - could be done by copyRemoteArtifacts but mapper arg expects java class
                sh "find . -mindepth 2 -type f -print -exec mv {} . \\;"

                //sh "rm *doc*.zip"
                sh "mv ${deliverabledir}-tck*.zip ${deliverabledir}-tck.zip"
            }
        } else {
            sh '''#!/bin/bash -ex
                cd ${WORKSPACE}/download 
                wget -q ${TCK_BUNDLE_URL} -O ${deliverabledir}-tck.zip
               '''
        }
    }

    stage("Unzip TCK and GF") {
        sh '''#!/bin/bash -ex
            cd ${WORKSPACE}
            unzip ${WORKSPACE}/download/glassfish.zip
            unzip -q ${WORKSPACE}/download/${deliverabledir}-tck.zip
            ls
            ls ${tck_root}
            
            cd ${WORKSPACE}/glassfish5/glassfish/modules
            jar xf jersey-common.jar META-INF/MANIFEST.MF
            cat  META-INF/MANIFEST.MF | grep Bundle-Version
           '''
    }

    stage ("Replace ts.jte") {
        sh '''#!/bin/bash -ex
            ls ${TS_HOME}
            ls ${TS_HOME}/bin
            yes | cp -rfv ${WORKSPACE}/download/ts.jte ${TS_HOME}/bin/ts.jte
           '''
    }

    stage("Configure TCK") {
        sh '''#!/bin/bash -ex
            cd ${TS_HOME}/bin
            ant config.vi
           '''
    }

    stage ("Deploy TCK tests") {
         sh '''#!/bin/bash -ex
            cd ${TS_HOME}/bin
            ant deploy.all
           '''
    }

    stage ("Run TCK tests") {
         sh '''#!/bin/bash -ex
            cd ${TS_HOME}/bin
            ant run.all | tee run.log
           '''
    }

    stage ("Create summary.txt, API, and run.log artifacts") {
        sh '''#!/bin/bash -ex
            cd ${TS_HOME}/bin
            cat run.log | sed -e '1,/Completed running/d' > summary.txt
            PASSED_COUNT=`head -1 summary.txt | tail -1 | sed 's/.*=\\s\\(.*\\)/\\1/'`
            FAILED_COUNT=`head -2 summary.txt | tail -1 | sed 's/.*=\\s\\(.*\\)/\\1/'`
            ERROR_COUNT=`head -3 summary.txt | tail -1 | sed 's/.*=\\s\\(.*\\)/\\1/'`
            
            echo ERROR_COUNT=${ERROR_COUNT}
            echo FAILED_COUNT=${FAILED_COUNT}
            echo PASSED_COUNT=${PASSED_COUNT}            
           '''

        archiveArtifacts artifacts: "${env.tck_root}/bin/summary.txt", fingerprint: true
        archiveArtifacts artifacts: "${env.tck_root}/bin/run.log", fingerprint: true
        archiveArtifacts artifacts: "glassfish5/glassfish/modules/${API_JAR_NAME}", fingerprint: true
        archiveArtifacts artifacts: "glassfish5/glassfish/modules/META-INF/MANIFEST.MF", fingerprint: true
        archiveArtifacts artifacts: "glassfish5/glassfish/domains/domain1/logs/server.log", fingerprint: true
    }
}