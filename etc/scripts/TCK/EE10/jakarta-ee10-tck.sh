#!/bin/bash
#
# Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

#
# script for listing staging from the staging repo.
#
# Input Parameters:
#     JERSEY_VERSION     - type: String
#                        - example: 3.1.5
#                        - description: version of Jersey (EE10) to be tested
#     GLASSFISH_VERSION  - type: String
#                        - example: 7.0.6
#                        - description: version of GF (EE10) to be used within tests
#     TCK_VERSION        - type: String
#                        - example: 3.1.3
#                        - description: version of a TCK bundle which will be downloaded from the staging/central
# Configuration:
#
# JDK:
#       openjdk-jdk11-latest
# Git:
#     https://github.com/eclipse-ee4j/jersey
#  Branch:
#     */3.1
#
#

#
# The first sh invocation
#
#!/bin/bash -xe

MVN_HOME="/opt/tools/apache-maven/latest/"
PATH="${MVN_HOME}/bin:${JAVA_HOME}:/bin:${PATH}"

mvn -f tests/jersey-tck/pom.xml clean install \
    -Dtck.version=${TCK_VERSION} \
    -Dglassfish.container.version=${GLASSFISH_VERSION} \
    -Djersey.version=${JERSEY_VERSION} | tee tck.log


#
# The second sh invocation
#
#!/bin/bash -xe

MVN_HOME="/opt/tools/apache-maven/latest/"
PATH="${MVN_HOME}/bin:${JAVA_HOME}:/bin:${PATH}"

mvn -f tests/jersey-tck/pom.xml dependency:copy -Dartifact=jakarta.ws.rs:jakarta-restful-ws-tck:${TCK_VERSION} -Dtck.version=${TCK_VERSION} -Dtransitive=false
export DOWNLOAD_PATH='tests/jersey-tck/target/dependency/'
export NAME=`ls ${DOWNLOAD_PATH}`

echo '***********************************************************************************' >> SUMMARY.TXT
echo '***                        TCK bundle information                               ***' >> SUMMARY.TXT
echo "*** Name:         ${NAME}                            ***" >> SUMMARY.TXT
echo '*** Artifact ID:	   jakarta.ws.rs:jakarta-restful-ws-tck:'${TCK_VERSION}'         ***' >> SUMMARY.TXT
echo '*** Date and size: '`stat -c "date: %y, size(b): %s" ${DOWNLOAD_PATH}/${NAME}`'  ***' >> SUMMARY.TXT
echo '*** SHA256SUM: '`sha256sum ${DOWNLOAD_PATH}/${NAME} | awk '{print $1}'`' ***' >> SUMMARY.TXT
echo '***                                                                             ***' >> SUMMARY.TXT
echo '***********************************************************************************' >> SUMMARY.TXT
echo '***                        TCK results summary                                  ***' >> SUMMARY.TXT
export TESTS_RUN=`grep 'Skipped: [0-9]*$' tck.log | grep -o 'Tests run: [0-9]*' | awk '{  SUM += $3 } END { print SUM }'`
export FAILURES=`grep 'Skipped: [0-9]*$' tck.log | grep -o 'Failures: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
export ERRORS=`grep 'Skipped: [0-9]*$' tck.log | grep -o 'Errors: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
export SKIPPED=`grep 'Skipped: [0-9]*$' tck.log | grep -o 'Skipped: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
echo '[INFO] Number of Tests Passed      = '${TESTS_RUN} >> SUMMARY.TXT
echo '[INFO] Number of Tests Failed      = '${FAILURES} >> SUMMARY.TXT
echo '[INFO] Number of Tests with Errors = '${ERRORS} >> SUMMARY.TXT
echo '[INFO] Number of Skipped Tests     = '${SKIPPED} >> SUMMARY.TXT
echo '[INFO] ****************************************************************************' >> SUMMARY.TXT
grep 'Tests run:' tck.log | grep --invert-match 'Skipped: [0-9]*$' >> SUMMARY.TXT


#
# Archive the artifacts:
#    tck.log,tests/jersey-tck/target/glassfish7/glassfish/domains/domain1/logs/server.log,tests/jersey-tck/pom.xml,SUMMARY.TXT
#