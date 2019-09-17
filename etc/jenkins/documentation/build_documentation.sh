#!/bin/bash -lex

#
# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

#Environment paths for Jenkins

#TOOLS_PREFIX=/opt/tools
#JAVA_PREFIX=/opt/tools/java/oracle
#MVN_HOME=/opt/tools/apache-maven/latest
#JAVA_HOME=/opt/tools/java/oracle/jdk-8/latest
#PATH=/opt/tools/apache-maven/latest/bin:/opt/tools/java/oracle/jdk-8/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# script shall be run before documentation generation to build jersey, site for jersey and docbook. Ideally it shall be run from Jenkins but
# if Jenkins does not provide required tools - java, maven, python, perl - to generate documentation the script can be run from local host.

# IMPORTANT - modify environmen.sh before running the script.
source environment.sh

mvn clean install -V -q -PtestsSkip,checkstyleSkip -Dtests.excluded '-Dmaven.test.skip=true'

mvn clean site -V -q -PtestsSkip,checkstyleSkip -Dtests.excluded -Dtests.excluded -Djavadoc.stylesheet=etc/config/javadoc-stylesheet.css -Dmaven.test.skip=true -Ddependency.locations.enabled=false -Dbundles.excluded -Djersey.version=${RELEASE_VERSION}

mvn clean install -V -q -e -U -B -f docs/pom.xml -Djersey.version=$RELEASE_VERSION