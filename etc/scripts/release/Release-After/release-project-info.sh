#!/bin/bash -lex
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
# script for project info publishing. MUST BE RUN ONLY AFTER THE APIDOCS PUBLISHING.
#
# Input Parameters:
#   RELEASE_VERSION    - type: String
#                      - example: 3.1.6
#                      - description: version for which project info is being published
#   DRY_RUN            - type: Boolean
#                      - description: If checked nothing is being published/generated
#   WEBSITE_URL        - type: String
#                      - value: git@github.com:eclipse-ee4j/jersey.github.io.git
#                      - description: GitHub url for the project info/apidocs repository.
#                        Mandatory and changes only in exceptional cases
# Configuration:
#
# JDK:
#       openjdk-jdk11-latest
# Git:
#       https://github.com/eclipse-ee4j/jersey
#
#   Branches to build:
#       tags/${RELEASE_VERSION}
#
# SSH agent:
#    GitHub bot SSH
#
#
#


#
# The first shell execution
#

MVN_HOME=/opt/tools/apache-maven/latest
PATH=${PATH}:${MVN_HOME}/bin:${JAVA_HOME}/bin

mvn clean install -B -V -q -PtestsSkip,checkstyleSkip -Dtests.excluded -DskipTests '-Dmaven.test.skip=true'

mvn -B -V -q -Pproject-info,checkstyleSkip,testsSkip,findbugsSkip,staging site site:stage \
     -DgenerateProjectInfo=true -Dtests.excluded -Ddependency.locations.enabled=false \
     -Dmaven.jxr.skip=true -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -DskipTests \
     -Dfindbugs.skip=true

#
# The second shell execution
#

#!/bin/bash -lex

export PATH=/opt/csw/bin:$PATH
WEB_DIR=$WORKSPACE/target/jersey-web
PROJECT_INFO_DIR=$WEB_DIR/project-info/$RELEASE_VERSION

if test -e $WEB_DIR ; then
    rm -rf $WEB_DIR
fi

# would couse shallow reject: git clone --depth 1 $WEBSITE_SOURCE_URL $WEB_DIR
git clone $WEBSITE_URL $WEB_DIR
cd $WEB_DIR

if test ! -e $PROJECT_INFO_DIR ; then
    mkdir -p $PROJECT_INFO_DIR
fi

cd $PROJECT_INFO_DIR

rm -rf jersey || true
cp -R $WORKSPACE/target/staging ./jersey

cd $WEB_DIR

git config --local user.email "jersey-bot@eclipse.org"
git config --local user.name "jersey-bot"
git add -A .
#git diff --cached --exit-code ||
git commit -m "[jenkins] automatic project-info update [$RELEASE_VERSION]"

if [ "$DRY_RUN" = "false" ]; then
  echo Pushing Web sources to $WEBSITE_URL
  git push origin master
else
  echo "Dry run .. not pushing to the master"
  git push origin master --dry-run
fi