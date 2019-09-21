#!/bin/sh
#
# Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

set -e
[ "$DEBUG" = "true" ] && set -x

# redeclaration of env variables so that editors do not think every variable is a typo
WAR_PATH=$WAR_PATH
APPLICATION_NAME=$APPLICATION_NAME
CONTEXT_ROOT=$CONTEXT_ROOT
REQUEST_PATH_QUERY=$REQUEST_PATH_QUERY
SKIP_REDEPLOY=$SKIP_REDEPLOY

if [ "$CONTEXT_ROOT" = "" -o "$WAR_PATH" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

ab -n50 -c5 "http://localhost:$PORT/$REQUEST_PATH_QUERY"

all_proxy="" http_proxy="" curl -sS "http://tomcat:tomcat@localhost:$PORT/manager/text/undeploy?path=/$CONTEXT_ROOT"

if [ "$SKIP_REDEPLOY" = "true" ]; then
    echo Skipping redeploy.
    exit
fi

all_proxy="" http_proxy="" curl -sS --upload-file "$WAR_PATH" "http://tomcat:tomcat@localhost:$PORT/manager/text/deploy?path=/$CONTEXT_ROOT&tag=$APPLICATION_NAME"

EXIT_CODE=$?
echo Redeployment finished with $EXIT_CODE

exit $EXIT_CODE
