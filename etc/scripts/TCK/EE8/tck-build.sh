#!/bin/bash -ex
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
# script for building EE8 TCK bundle for Jersey.
#
# Input Parameters:
#   GF_URL     - type: String
#              - value: https://search.maven.org/remotecontent?filepath=org/glassfish/main/distributions/glassfish/5.1.0/glassfish-5.1.0.zip
#              - description: actual URL of the Glassfish 5
#
# Configuration:
#
# JDK:
#       oracle-jdk8-latest
# Git:
#     https://github.com/eclipse-ee4j/jakartaee-tck.git
#  Branch:
#     */master
#  Check out to a sub-directory:
#     CTS
#
#  [X] With Ant:
#       apache-ant-latest
#     JDK:
#       oracle-jdk8-latest
#
#  Copy artifacts from another project:
#       ts_jte_alter
#    Latest successful build
#    Artifacts to copy:
#       ts.jte
#    Target directory:
#       download
#    [X] Flatten directories [X] Fingerprint Artifacts
#
#
#

mkdir ${WORKSPACE}/CTS/tools
mkdir ${WORKSPACE}/CTS/tools/ant

#create zip command
#mkdir batch
#touch ${WORKSPACE}/batch/zip.sh
#chmod +x ${WORKSPACE}/batch/zip.sh
#touch ${WORKSPACE}/batch/zip
#chmod +x ${WORKSPACE}/batch/zip
#alias zip='${WORKSPACE}/batch/zip.sh'

#echo "echo ARG1=\$1">batch/zip.sh
#echo "echo ARG2=\$2">>batch/zip.sh
#echo "echo ARG3=\$3">>batch/zip.sh
#echo "if [ \$1='-T' ]; then">>batch/zip.sh
#echo "	exit">>batch/zip.sh
#echo "fi">>batch/zip.sh
#echo "filename=\`echo \$2 | cut -d'.' -f 1,2\`">>batch/zip.sh
#echo "echo filename=\$filename">>batch/zip.sh
#echo "tar -zcvf '\${filename}'.tar.gz \$3">>batch/zip.sh
#echo "mv \${filename}.tar.gz \${filename}.zip">>batch/zip.sh
#echo "echo created \${filename}.zip">>batch/zip.sh

#echo "${WORKSPACE}/batch/zip.sh \$*">batch/zip

#cat batch/zip.sh
#cat batch/zip


#Set variables
export ANT_OPTS=-Djavax.xml.accessExternalSchema=all

cd download
#wget -4 https://jenkins.eclipse.org/jersey/view/TCK/job/ts_jte_alter/lastSuccessfulBuild/artifact/ts.jte

#wget -q https://ci.adoptopenjdk.net/view/Build%20Monitor/job/jtharness/lastSuccessfulBuild/artifact/jtharness.tar.gz -O jtharness.tar.gz
#wget -q https://ci.adoptopenjdk.net/view/Build%20Monitor/job/sigtest/lastSuccessfulBuild/artifact/sigtest.tar.gz -O sigtest.tar.gz
#wget -q http://central.maven.org/maven2/com/sun/xml/bind/jaxb-xjc/2.2.7/jaxb-xjc-2.2.7.jar -O jaxb-xjc.jar
#wget -q http://central.maven.org/maven2/ant-contrib/ant-contrib/1.0b3/ant-contrib-1.0b3.jar -O ant-contrib.jar
#wget -q http://central.maven.org/maven2/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar
#wget -q http://central.maven.org/maven2/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar
#wget -q http://central.maven.org/maven2/commons-codec/commons-codec/1.3/commons-codec-1.3.jar
#tar xfz jtharness.tar.gz
#tar xvfz sigtest.tar.gz
#unzip -q sigtest-4.0
#cp -av ${WORKSPACE}/download/sigtest-4.0/lib/. ${WORKSPACE}/CTS/lib/
#cp -v ${WORKSPACE}/download/jtharness/lib/javatest.jar ${WORKSPACE}/CTS/lib/javatest.jar
#cp -v ${WORKSPACE}/download/jaxb-xjc.jar ${WORKSPACE}/CTS/lib/
#cp -v ${WORKSPACE}/download/ant-contrib.jar ${WORKSPACE}/CTS/lib/
#cp -v ${WORKSPACE}/download/ant-contrib.jar ${WORKSPACE}/CTS/lib/ant-contrib-1.0b3.jar
#cp -v ${WORKSPACE}/download/commons-httpclient-3.1.jar ${WORKSPACE}/CTS/lib/
#cp -v ${WORKSPACE}/download/commons-logging-1.1.1.jar ${WORKSPACE}/CTS/lib/
#cp -v ${WORKSPACE}/download/commons-codec-1.3.jar ${WORKSPACE}/CTS/lib/


ls

yes | cp -vr ${WORKSPACE}/download/ts.jte ${WORKSPACE}/CTS/install/jaxrs/bin/
#cp -v ${WORKSPACE}/download/ts.jte ${WORKSPACE}/CTS/bin/ts.jte

wget -q ${GF_URL} -O glassfish.zip
unzip -q glassfish.zip -d ${WORKSPACE}

#COMPILE TCK

export TS_HOME=${WORKSPACE}/CTS
export deliverabledir=jaxrs
export javaee_home=${WORKSPACE}/glassfish5

#touch ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/api = servlet >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/api/rs/ext/interceptor = standalone >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/ee = standalone >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/jaxrs21 = standalone >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/jaxrs21/api = servlet >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/spec = standalone >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/servlet3 = standalone >> ${WORKSPACE}/CTS/vehicle.properties
#echo com/sun/ts/tests/jaxrs/platform  = standalone >> ${WORKSPACE}/CTS/vehicle.properties

#cat ${WORKSPACE}/CTS/vehicle.properties

#fix the test
cd ${WORKSPACE}/CTS/src/com/sun/ts/tests/jaxrs/jaxrs21/ee/sse/sseeventsink
sed -i '314s/open()/register(holder::add)/' JAXRSClient.java
sed -i '315s/register(holder::add)/open()/' JAXRSClient.java
#end of fix

cd ${WORKSPACE}/CTS/install/${deliverabledir}/bin
ant build.all
ant update.jaxrs.wars

# BUNDLE TCK

cd ${WORKSPACE}/CTS/release/tools/
ant jakartaee-jaxrs
#ls ${WORKSPACE}/CTS/release/JAXRS_BUILD/
cd ${WORKSPACE}/CTS/release/JAXRS_BUILD/latest
for fn in `ls *.zip`; do cp -v "${fn}" `echo ${fn} | cut -d'_' -f 1`_latest.zip; done

#zip -s 10m jaxrstck_split_latest.zip jaxrstck-2.1_latest.zip
#ls



#
#  Archive the artifacts:
#    CTS/release/JAXRS_BUILD/latest/*.z*
#
#

