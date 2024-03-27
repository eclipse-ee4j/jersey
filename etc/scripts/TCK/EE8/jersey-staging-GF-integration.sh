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
# script for Jersey EE8 integration into Glassfish 5.
#
# Input Parameters:
#   GF_URL           - type: String
#                    - value: http://central.maven.org/maven2/org/glassfish/main/distributions/glassfish/5.1.0/glassfish-5.1.0.zip
#                    - description: actual URL of the Glassfish 5
#   MAVEN_REPO_URL   - type: String
#                    - value: https://jakarta.oss.sonatype.org/content/groups/staging/org/glassfish/jersey
#                    - description: Jersey binaries location
#   JERSEY_VERSION   - type: String
#                    - value: 2.42
#                    - description: the version of Jersey to be integrated into the GF
#   JERSEY_JARS      - type: Multi-line String
#                    - value: containers/glassfish/jersey-gf-ejb/${JERSEY_VERSION}/jersey-gf-ejb
#                              containers/jersey-container-grizzly2-http/${JERSEY_VERSION}/jersey-container-grizzly2-http
#                              containers/jersey-container-servlet/${JERSEY_VERSION}/jersey-container-servlet
#                              containers/jersey-container-servlet-core/${JERSEY_VERSION}/jersey-container-servlet-core
#                              core/jersey-client/${JERSEY_VERSION}/jersey-client
#                              core/jersey-common/${JERSEY_VERSION}/jersey-common
#                              core/jersey-server/${JERSEY_VERSION}/jersey-server
#                              ext/jersey-bean-validation/${JERSEY_VERSION}/jersey-bean-validation
#                              ext/cdi/jersey-cdi1x/${JERSEY_VERSION}/jersey-cdi1x
#                              ext/cdi/jersey-cdi1x-servlet/${JERSEY_VERSION}/jersey-cdi1x-servlet
#                              ext/cdi/jersey-cdi1x-transaction/${JERSEY_VERSION}/jersey-cdi1x-transaction
#                              ext/jersey-entity-filtering/${JERSEY_VERSION}/jersey-entity-filtering
#                              ext/jersey-mvc/${JERSEY_VERSION}/jersey-mvc
#                              ext/jersey-mvc-jsp/${JERSEY_VERSION}/jersey-mvc-jsp
#                              inject/jersey-hk2/${JERSEY_VERSION}/jersey-hk2
#                              media/jersey-media-jaxb/${JERSEY_VERSION}/jersey-media-jaxb
#                              media/jersey-media-json-binding/${JERSEY_VERSION}/jersey-media-json-binding
#                              media/jersey-media-json-jackson/${JERSEY_VERSION}/jersey-media-json-jackson
#                              media/jersey-media-json-jettison/${JERSEY_VERSION}/jersey-media-json-jettison
#                              media/jersey-media-json-processing/${JERSEY_VERSION}/jersey-media-json-processing
#                              media/jersey-media-moxy/${JERSEY_VERSION}/jersey-media-moxy
#                              media/jersey-media-multipart/${JERSEY_VERSION}/jersey-media-multipart
#                              media/jersey-media-sse/${JERSEY_VERSION}/jersey-media-sse
#                    - description: List of binaries to be integrated into the GF 5
#
#
# Configuration:
#
# JDK:
#       (System)
# Git:
#     ------none------
#
#  Copy artifacts from another project:
#       JAXRS_build
#    Latest successful build
#    Artifacts to copy:
#       **/*.jar
#    Target directory:
#       download
#    [X] Flatten directories [X] Fingerprint Artifacts
#
#
#
ls
cd download
wget -q ${GF_URL} -O glassfish.zip
#wget -q ${JAX_RS_JAR} -O jakarta.ws.rs-api.jar

while IFS= read -r line ; do wget -q ${MAVEN_REPO_URL}/$line-${JERSEY_VERSION}.jar; done <<< "${JERSEY_JARS}"

echo Listing grabbed jars
ls *.jar

#unzip
unzip -q glassfish.zip -d ${WORKSPACE}
cd ${WORKSPACE}/glassfish5/glassfish/modules

#replace API jar
#cp -v ${WORKSPACE}/download/jakarta.ws.rs-api.jar .

#replace Jersey Jar
for jarfile in ${WORKSPACE}/download/*.jar; do
   echo $(basename $jarfile) | sed -e 's/-RC[0-9][0-9]*//' | sed -e 's/-SNAPSHOT//' | sed -e 's/\.[0-9][0-9]*//' | sed -e 's/\.[0-9][0-9]*//' | sed -e 's/-[0-9][0-9]*//' | while IFS= read -r gfnamejar ; do if [ -f $gfnamejar ]; then rm -v $gfnamejar; cp -v $jarfile $gfnamejar; fi; done;
done

cd ${WORKSPACE}
zip -r glassfish.zip glassfish5


#
#  Archive the artifacts:
#    glassfish.zip
#
#
