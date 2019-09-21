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
WAR_PATH="$WAR_PATH"
AS_HOME="$AS_HOME"
ADMINPORT=$ADMINPORT
APPLICATION_NAME=$APPLICATION_NAME
CONTEXT_ROOT=$CONTEXT_ROOT
SKIP_REDEPLOY=$SKIP_REDEPLOY

if [ "$AS_HOME" = "" -o "$WAR_PATH" = "" -o "$ADMINPORT" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

chmod +x "$AS_HOME"/bin/asadmin

"$AS_HOME"/bin/asadmin undeploy --port $ADMINPORT $APPLICATION_NAME

if [ "$SKIP_REDEPLOY" = "true" ]; then
    echo Skipping redeploy.
    exit
fi

"$AS_HOME"/bin/asadmin deploy --port $ADMINPORT --contextroot $CONTEXT_ROOT --name $APPLICATION_NAME "$WAR_PATH"

EXIT_CODE=$?
echo Redeployment finished with $EXIT_CODE

exit $EXIT_CODE
