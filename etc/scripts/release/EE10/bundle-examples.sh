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
# script for releasing examples bundle by release tag.
#
# Input Parameters:
#
# VERSION - type: String
#         - example: 3.1.6
#         - description: Name of the existing version tag in the Git repo.
#
# Configuration:
#
# JDK:
#       oracle-jdk11-latest
# Git:
#          git@github.com:eclipse-ee4j/jersey.git
#    Branches to build:
#          tags/${VERSION}
#
# Bindings:
#    Secret file:
#         Variable:
#              KEYRING
#         Credentials:
#              secret-subkeys.asc
#
#


 gpg --batch --import ${KEYRING}
 for fpr in $(gpg --list-keys --with-colons  | awk -F: '/fpr:/ {print $10}' | sort -u);
 do
   echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key $fpr trust;
 done


 # Execution environment
 MVN_HOME="/opt/tools/apache-maven/latest/"
 PATH="${MVN_HOME}/bin:${JAVA_HOME}:/bin:${PATH}"

 mvn clean install -C -B -Djersey.version=${VERSION} -DskipTests -Ppre-release,staging -pl org.glassfish.jersey.examples:project -amd

 mvn -C -B -DstagingDescription="org.glassfish.jersey.examples:${VERSION}" \
     -Pstaging,oss-release,pre-release \
     -Djersey.version=${VERSION} -DskipTests -pl :jersey-examples \
     deploy