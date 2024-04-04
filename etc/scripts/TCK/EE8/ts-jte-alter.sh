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
# script for generating ts.jte for EE8 Jersey TCK.
#
# Input Parameters:
#
#   SERVER_HOME         - type: String
#                       - value: ${ts.home}/../glassfish5/glassfish
#  JAXRS_API_JAR_NAME   - type: String
#                       - value: jakarta.ws.rs-api.jar
#  HARNESS_REPORT_DIR   - type: String
#                       - value: ${ts.home}/../JTreport
#  HARNESS_WORK_DIR     - type: String
#                       - value: ${ts.home}/../JTwork
#
# Configuration:
#
# JDK:
#       (System)
# Git:
#     ----none----
#
#

wget https://raw.githubusercontent.com/eclipse-ee4j/jakartaee-tck/master/install/jaxrs/bin/ts.jte

JARS_ON_CP="\
\${web.home}/modules/jersey-client.jar:\
\${web.home}/modules/jersey-common.jar:\
\${web.home}/modules/jersey-container-servlet.jar:\
\${web.home}/modules/jersey-container-servlet-core.jar:\
\${web.home}/modules/jersey-hk2.jar:\
\${web.home}/modules/jersey-media-jaxb.jar:\
\${web.home}/modules/jersey-media-json-binding.jar:\
\${web.home}/modules/jersey-media-json-processing.jar:\
\${web.home}/modules/jersey-media-sse.jar:\
\${web.home}/modules/jersey-server.jar:\
\${web.home}/modules/jsonp-jaxrs.jar:\
\${web.home}/modules/asm-all-repackaged.jar:\
\${web.home}/modules/bean-validator.jar:\
\${web.home}/modules/endorsed/jakarta.annotation-api.jar:\
\${web.home}/modules/cdi-api.jar:\
\${web.home}/modules/cglib.jar:\
\${web.home}/modules/hk2-api.jar:\
\${web.home}/modules/hk2-locator.jar:\
\${web.home}/modules/hk2-utils.jar:\
\${web.home}/modules/javassist.jar:\
\${web.home}/modules/jakarta.ejb-api.jar:\
\${web.home}/modules/jakarta.inject.jar:\
\${web.home}/modules/jakarta.json.jar:\
\${web.home}/modules/jakarta.json-api.jar:\
\${web.home}/modules/jakarta.json.bind-api.jar:\
\${web.home}/modules/jakarta.interceptor-api.jar:\
\${web.home}/modules/jakarta.servlet-api.jar:\
\${web.home}/modules/osgi-resource-locator.jar:\
\${web.home}/modules/weld-osgi-bundle.jar:\
\${web.home}/modules/yasson.jar"

sed -i 's/^impl\.vi=/impl\.vi=glassfish/g' ts.jte
sed -i "s/^web\.home=/$(echo web\.home=${SERVER_HOME} | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^impl\.vi\.deploy\.dir=/$(echo impl\.vi\.deploy\.dir=\${web.home}/domains/domain1/autodeploy | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i 's/^jaxrs_impl_name=/jaxrs_impl_name=jersey/g' ts.jte
sed -i 's/^harness\.log\.traceflag=/harness\.log\.traceflag=true/g' ts.jte
sed -i 's/^webServerHost=/webServerHost=localhost/g' ts.jte
sed -i 's/^webServerPort=/webServerPort=8080/g' ts.jte
sed -i "s/^work\.dir=\/tmp\/JTwork/$(echo work\.dir=${HARNESS_WORK_DIR} | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^report\.dir=\/tmp\/JTreport/$(echo report\.dir=${HARNESS_REPORT_DIR} | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^jaxrs_impl\.classes=/$(echo jaxrs_impl\.classes=${JARS_ON_CP} | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^jaxrs\.classes=/$(echo jaxrs\.classes=\${web.home}/modules/${JAXRS_API_JAR_NAME} | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^jaxrs_impl_lib=/$(echo jaxrs_impl_lib=\${web.home}/modules/jersey-container-servlet-core.jar | sed -e 's/\\/\\\\/g; s/\//\\\//g;')/g" ts.jte
sed -i "s/^servlet_adaptor=/servlet_adaptor=org\/glassfish\/jersey\/servlet\/ServletContainer.class/g" ts.jte

sed -i "s/-Dcts.tmp=\.*/-Djavax.xml.accessExternalSchema=all -Dcts.tmp=/g" ts.jte


#
# Archive the artifacts:
#     ts.jte