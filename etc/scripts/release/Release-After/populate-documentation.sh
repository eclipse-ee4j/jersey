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
# script for api docs publishing. Publishes api docs from the bundle in the Maven Central to the project's site.
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
#  BRANCH_SPECIFIER    - type: String
#                      - example: 2.x
#                      - description: Branch for which the api docs are being published. Used only in Git commit message
#  UPDATE_LATEST       - type: Boolean
#                      - description: If checked updates the latest api docs (distinguishes for EE8/EE9/EE10) along
#                        with publication into the RELEASE_VERSION folder.
# Configuration:
#
# JDK:
#       oracle-jdk8-latest
# Git:
#       ----none----
#
# SSH agent:
#    GitHub bot SSH
#
#
#


#
# The first shell execution
#


TOOLS_PREFIX=/opt/tools
#JAVA_PREFIX=/opt/tools/java/oracle
MVN_HOME=/opt/tools/apache-maven/latest
#JAVA_HOME=/opt/tools/java/oracle/jdk-8/latest
PATH=/opt/tools/apache-maven/latest/bin:${JAVA_HOME}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

export CENTRAL_URL='https://repo1.maven.org/maven2'
export STAGING_URL='https://jakarta.oss.sonatype.org/content/repositories/staging'

# !!! if build docbook it's essentially important to turn the pre-release profile ON !!!
#mvn clean install -Pstaging,pre-release -DskipTests  -V -q -e -U -B -f docs/pom.xml -Djersey.version=$RELEASE_VERSION
# however we download already published docbook from staging/central:
mkdir -p $WORKSPACE/docs/target/docbook/index
cd $WORKSPACE/docs/target/docbook/index
wget -nv ${STAGING_URL}/org/glassfish/jersey/jersey-documentation/${RELEASE_VERSION}/jersey-documentation-${RELEASE_VERSION}-docbook.zip
unzip -o jersey-documentation-${RELEASE_VERSION}-docbook.zip
rm jersey-documentation-${RELEASE_VERSION}-docbook.zip

mkdir -p $WORKSPACE/target/site/apidocs
cd $WORKSPACE/target/site/apidocs
wget -nv ${STAGING_URL}/org/glassfish/jersey/bundles/apidocs/${RELEASE_VERSION}/apidocs-${RELEASE_VERSION}-javadoc.jar -O apidocs-javadoc.jar
jar -xf apidocs-javadoc.jar
rm apidocs-javadoc.jar
rm -rf META-INF


#
# The second shell execution
#


#!/bin/bash -lex

TOOLS_PREFIX=/opt/tools
#JAVA_PREFIX=/opt/tools/java/oracle
MVN_HOME=/opt/tools/apache-maven/latest
#JAVA_HOME=/opt/tools/java/oracle/jdk-8/latest
PATH=/opt/tools/apache-maven/latest/bin:${JAVA_HOME}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

DIRS="$RELEASE_VERSION"
if $UPDATE_LATEST;
then
	if [[ $RELEASE_VERSION == 2* ]] ;
    then
    	export DIRS="latest $DIRS"
    elif [[ $RELEASE_VERSION == 3.1* ]] ;
    then
    	export DIRS="latest31x $DIRS"
    else
    	export DIRS="latest30x $DIRS"
    fi
    echo ${DIRS} for ${RELEASE_VERSION} release
fi

#export PATH=/opt/csw/bin:$PATH
WEB_DIR=$WORKSPACE/target/jersey-web


function copyDocs {
    APIDOCS_DIR=$WEB_DIR/apidocs/$1
    DOCS_DIR=$WEB_DIR/documentation/$1

    #
    # API docs
    #
    if test ! -e $APIDOCS_DIR ; then
        mkdir -p $APIDOCS_DIR
    fi
    cd $APIDOCS_DIR

    rm -rf jersey || true
    cp -R $WORKSPACE/target/site/apidocs ./jersey

    #
    # user guide
    #
    rm -rf $DOCS_DIR || true
    mkdir -p $DOCS_DIR
    cp -r $WORKSPACE/docs/target/docbook/index/* $DOCS_DIR
    rm $DOCS_DIR/*.fo || true
}

if test -e $WEB_DIR ; then
    rm -rf $WEB_DIR
fi

# would couse shallow reject: git clone --depth 1 $WEBSITE_URL $WEB_DIR
git clone $WEBSITE_URL $WEB_DIR

cd $WEB_DIR

for dir in $DIRS; do
    copyDocs $dir
done

cd $WEB_DIR

git config --local user.email "jersey-bot@eclipse.org"
git config --local user.name "jersey-bot"
git add -A .
git diff --cached --exit-code || git commit -m "[jenkins] automatic javadoc and documentation update [$RELEASE_VERSION @ $BRANCH_SPECIFIER]"

if [ "$DRY_RUN" = "false" ]; then
  echo "Pushing Web sources to $WEBSITE_URL"
  git push origin master
else
  echo "Dry run .. not pushing to the master"
  git push origin master --dry-run
fi


