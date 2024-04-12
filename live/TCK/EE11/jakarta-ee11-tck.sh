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

[[ -z ${1} ]] && SUMMARY_FILE_NAME='SUMMARY.TXT' || SUMMARY_FILE_NAME=${1}
[[ -z ${2} ]] && TCK_LOG_FILE_NAME='tck.log' || TCK_LOG_FILE_NAME=${2}

#Build the Tomcat Arquillian
mkdir Arquillian
cd Arquillian
git clone https://github.com/arquillian/arquillian-container-tomcat.git .
mvn -V clean install -DskipTests

cd ${WORKSPACE}

mkdir TCK
cd TCK
export tckname=$(basename "${TCK_BUNDLE}")
echo ${tckname}
wget -q ${TCK_BUNDLE} -O ${tckname}
ls
jar xvf ${tckname}

filename="${tckname%.*}"
echo ${filename}

cd restful-ws-tck/artifacts
ls

mvn install:install-file \
   -Dfile=${filename}.jar \
   -DgroupId=jakarta.ws.rs \
   -DartifactId=jakarta-restful-ws-tck \
   -Dversion=${TCK_VERSION} \
   -Dpackaging=jar \
   -DgeneratePom=true

cd ${WORKSPACE}

#Temporary
sed -i "s#<jersey.version>4.0.99-SNAPSHOT</jersey.version>#<jersey.version>${JERSEY_VERSION}</jersey.version>#p" tests/jersey-tck/pom.tomcat.xml
cat tests/jersey-tck/pom.tomcat.xml

mvn -f tests/jersey-tck/pom.tomcat.xml clean install -Pstaging -Djersey.version=${JERSEY_VERSION} | tee ${TCK_LOG_FILE_NAME}

mvn -f tests/jersey-tck/pom.tomcat.xml dependency:copy -Dartifact=jakarta.ws.rs:jakarta-restful-ws-tck:${TCK_VERSION} -Dtck.version=${TCK_VERSION} -Dtransitive=false
#export DOWNLOAD_PATH='tests/jersey-tck/target/dependency'
#export NAME=`ls ${DOWNLOAD_PATH}`
export DOWNLOAD_PATH=${WORKSPACE}/TCK
export NAME=$(basename "${TCK_BUNDLE}")

echo '***********************************************************************************' >> ${SUMMARY_FILE_NAME}
echo '***                        TCK bundle information                               ***' >> ${SUMMARY_FILE_NAME}
echo "*** Name:         ${NAME}                            ***" >> ${SUMMARY_FILE_NAME}
echo '*** Artifact ID:	   jakarta.websocket:websocket-tck-spec-tests:'${TCK_VERSION}'         ***' >> ${SUMMARY_FILE_NAME}
echo '*** Date and size: '`stat -c "date: %y, size(b): %s" ${DOWNLOAD_PATH}/${NAME}`'  ***' >> ${SUMMARY_FILE_NAME}
echo '*** SHA256SUM: '`sha256sum ${DOWNLOAD_PATH}/${NAME} | awk '{print $1}'`' ***' >> ${SUMMARY_FILE_NAME}
echo '***                                                                             ***' >> ${SUMMARY_FILE_NAME}
echo '***********************************************************************************' >> ${SUMMARY_FILE_NAME}
echo '***                        TCK results summary                                  ***' >> ${SUMMARY_FILE_NAME}
export TESTS_RUN=`grep 'Skipped: [0-9]*$' ${TCK_LOG_FILE_NAME} | grep -o 'Tests run: [0-9]*' | awk '{  SUM += $3 } END { print SUM }'`
export FAILURES=`grep 'Skipped: [0-9]*$' ${TCK_LOG_FILE_NAME} | grep -o 'Failures: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
export ERRORS=`grep 'Skipped: [0-9]*$' ${TCK_LOG_FILE_NAME} | grep -o 'Errors: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
export SKIPPED=`grep 'Skipped: [0-9]*$' ${TCK_LOG_FILE_NAME} | grep -o 'Skipped: [0-9]*' | awk '{  SUM += $2 } END { print SUM }'`
echo '[INFO] Number of Tests Passed      = '${TESTS_RUN} >> ${SUMMARY_FILE_NAME}
echo '[INFO] Number of Tests Failed      = '${FAILURES} >> ${SUMMARY_FILE_NAME}
echo '[INFO] Number of Tests with Errors = '${ERRORS} >> ${SUMMARY_FILE_NAME}
echo '[INFO] Number of Skipped Tests     = '${SKIPPED} >> ${SUMMARY_FILE_NAME}
echo '[INFO] ****************************************************************************' >> ${SUMMARY_FILE_NAME}
grep 'Tests run:' ${TCK_LOG_FILE_NAME} | grep --invert-match 'Skipped: [0-9]*$' >> ${SUMMARY_FILE_NAME}