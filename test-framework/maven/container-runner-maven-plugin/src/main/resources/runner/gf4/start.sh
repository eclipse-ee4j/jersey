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
MAX_HEAP=$MAX_HEAP
TRIES_COUNT=$TRIES_COUNT
AS_HOME=$AS_HOME
PORT=$PORT
ADMINPORT=$ADMINPORT
SKIP_DEPLOY=$SKIP_DEPLOY
DOMAIN=$DOMAIN
APPLICATION_NAME=$APPLICATION_NAME
CONTEXT_ROOT=$CONTEXT_ROOT
JVM_ARGS=$JVM_ARGS
SKIP_START_STOP=$SKIP_START_STOP
SKIP_CHECK=$SKIP_CHECK

if [ "$AS_HOME" = "" -o "$WAR_PATH" = "" -o "$MAX_HEAP" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

chmod +x "$AS_HOME"/bin/asadmin

# Start Glassfish
if [ "$SKIP_START_STOP" = "true" ]; then
    echo Start skipped
else
    if [ "$SKIP_CHECK" != "true" ] && jps -v | grep 'jersey.config.test.memleak.gf4.magicRunnerIdentifier'; then
        echo ERROR There is already running instance of Glassfish
        exit 2
    fi

    if nc -z localhost $PORT; then
        echo ERROR port $PORT is not free!
        exit 3
    fi

    [ -d "$AS_HOME"/domains/$DOMAIN ] && rm -rf "$AS_HOME"/domains/$DOMAIN

    "$AS_HOME"/bin/asadmin create-domain --adminport $ADMINPORT --instanceport $PORT --nopassword $DOMAIN
    "$AS_HOME"/bin/asadmin start-domain --port $ADMINPORT $DOMAIN

    set +e
    "$AS_HOME"/bin/asadmin delete-jvm-options --port $ADMINPORT --target default-config -Xmx512m
    "$AS_HOME"/bin/asadmin delete-jvm-options --port $ADMINPORT --target default-config -Xmx$MAX_HEAP

    "$AS_HOME"/bin/asadmin delete-jvm-options --port $ADMINPORT --target server-config -Xmx512m
    "$AS_HOME"/bin/asadmin delete-jvm-options --port $ADMINPORT --target server-config -Xmx$MAX_HEAP

    set -e
    "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target default-config -Xmx$MAX_HEAP
    "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target server-config -Xmx$MAX_HEAP

    # add magic runner identifier so that we can identify other processes
    "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target default-config -Djersey.config.test.memleak.gf4.magicRunnerIdentifier
    "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target server-config -Djersey.config.test.memleak.gf4.magicRunnerIdentifier

    # if JVM_ARGS doesn't contain following vm options, set them (increases the probability of OOME GC Overhead exceeded)
    echo "$JVM_ARGS" | grep GCTimeLimit > /dev/null || "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target server-config "-XX\:GCTimeLimit=20"
    echo "$JVM_ARGS" | grep GCHeapFreeLimit > /dev/null || "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target server-config "-XX\:GCHeapFreeLimit=30"

    if [ "$JVM_ARGS" != "" ]; then
        for JVM_ARG in `echo $JVM_ARGS`; do
            "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target default-config "$JVM_ARG"
            "$AS_HOME"/bin/asadmin create-jvm-options --port $ADMINPORT --target server-config "$JVM_ARG"
        done
    fi

    "$AS_HOME"/bin/asadmin stop-domain --port $ADMINPORT --force=true $DOMAIN
    "$AS_HOME"/bin/asadmin start-domain --port $ADMINPORT $DOMAIN
fi

# Deploy to Glassfish
if [ "$SKIP_DEPLOY" = "true" ]; then
    echo Deployment skipped
else
    "$AS_HOME"/bin/asadmin deploy --port $ADMINPORT --contextroot $CONTEXT_ROOT --name $APPLICATION_NAME "$WAR_PATH"
fi
