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
# script for publishing listed stagings from the staging repo into the Maven Central.
#
# Input Parameters:
#     STAGING_REPO_ID  - type: String
#                      - example: orgglassfishjersey-1226,orgglassfishjersey-1227,orgglassfishjersey-1228,orgglassfishjersey-1229,orgglassfishjersey-1230
#                      - description: list all staggings (comma separated) to be published to the Maven Central
#     STAGING_DESC     - type: String
#                      - example:org.glassfish.jersey:2.42
#                      - description: description of what is published. Usually <group.id>:<version>
# Configuration:
#
# JDK:
#       (System)
# Git:
#     ----none----
#
#

cat <<EOT >> pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.eclipse.ee4j</groupId>
        <artifactId>project</artifactId>
        <version>1.0.9</version>
        <relativePath/>
    </parent>

    <groupId>org.glassfish.jersey</groupId>
    <artifactId>release-helper-util</artifactId>
    <version>1.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Jersey Release Helper</name>

    <description>Generated POM for Jersey Release Helper utils</description>
    <url>https://projects.eclipse.org/projects/ee4j.jersey</url>

    <scm>
      <connection>scm:git:git://github.com/eclipse-ee4j/jersey</connection>
      <developerConnection>scm:git:git://github.com/eclipse-ee4j/jersey</developerConnection>
      <url>https://github.com/eclipse-ee4j/jersey</url>
    </scm>

</project>
EOT

MVN_HOME="/opt/tools/apache-maven/latest/"
PATH="${MVN_HOME}/bin:${JAVA_HOME}:/bin:${PATH}"

export STAGING_PARAMS='-DnexusUrl=https://jakarta.oss.sonatype.org/ -DserverId=ossrh'
export STAGING_PLUGIN='org.sonatype.plugins:nexus-staging-maven-plugin:1.6.7'


mvn ${STAGING_PARAMS} -B -C -V ${STAGING_PLUGIN}:rc-release -DstagingRepositoryId="${STAGING_REPO_ID}" -DstagingDescription="${STAGING_DESC}"

