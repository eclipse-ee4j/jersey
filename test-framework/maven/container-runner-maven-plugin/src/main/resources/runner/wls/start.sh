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
MW_HOME=$MW_HOME
DOMAIN=$DOMAIN
PORT=$PORT
SKIP_DEPLOY=$SKIP_DEPLOY
DOMAIN=$DOMAIN
APPLICATION_NAME=$APPLICATION_NAME
LOGFILE=$LOGFILE
CONTEXT_ROOT=$CONTEXT_ROOT
JVM_ARGS=$JVM_ARGS
SKIP_START_STOP=$SKIP_START_STOP
SKIP_CHECK=$SKIP_CHECK
DIST_DIR=$DIST_DIR

WLS_SERVER_NAME=$WLS_SERVER_NAME

DEPLOY_TIMEOUT=30000
CONNECT_TIMEOUT=30000

if [ "$WAR_PATH" = "" -o "$DOMAIN" = "" -o "$MW_HOME" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

export JAVA_OPTIONS=-Djava.endorsed.dirs="$JAVA_HOME/jre/lib/endorsed":"$MW_HOME/oracle_common/modules/endorsed"

set +x
. "$MW_HOME"/wlserver/server/bin/setWLSEnv.sh
[ "$DEBUG" = "true" ] && set -x

if [ "$SKIP_START_STOP" = "true" ]; then
    cd "$MW_HOME/$DOMAIN"
    echo Start skipped
else
    if [ "$SKIP_CHECK" != "true" ] && jps -v | grep 'jersey.config.test.memleak.wls.magicRunnerIdentifier'; then
        echo ERROR There is already running instance of Memleak Test Weblogic
        exit 2
    fi

    if nc -z localhost $PORT; then
        echo ERROR port $PORT is not free!
        exit 3
    fi

    rm -rf "$MW_HOME/$DOMAIN"
    mkdir "$MW_HOME/$DOMAIN"

    cd "$MW_HOME/$DOMAIN"

    java $JAVA_OPTIONS -Xmx$MAX_HEAP -XX:MaxPermSize=256m \
        -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$DIST_DIR \
        -XX:GCTimeLimit=20 -XX:GCHeapFreeLimit=30 \
        -Djersey.config.test.memleak.wls.magicRunnerIdentifier \
        -Dweblogic.management.GenerateDefaultConfig \
        -Dweblogic.Domain=$DOMAIN \
        -Dweblogic.Name=$WLS_SERVER_NAME \
        -Dweblogic.management.username=weblogic \
        -Dweblogic.management.password=weblogic1 \
        -Dweblogic.ListenPort=$PORT \
        -Djava.security.egd=file:/dev/./urandom \
        $JVM_ARGS \
        weblogic.Server > "$LOGFILE" 2>&1 &

    echo $! > "$MW_HOME/$DOMAIN/wls.pid"

    for A in `seq $TRIES_COUNT`; do
        set +e
        nc -z localhost $PORT && break
        set -e
        sleep 5
    done

    set -e
fi

if [ "$SKIP_DEPLOY" = "true" ]; then
    echo Deployment skipped
    exit 0
fi

# It is impossible to easilly set the context root in Weblogic besides changing the war name
RENAMED_WAR_PATH="$MW_HOME"/$CONTEXT_ROOT.${WAR_PATH##*.}
if [ "${WAR_PATH##*/}" != "${RENAMED_WAR_PATH##*/}" ]; then
    [ -f "$RENAMED_WAR_PATH" ] && rm "$RENAMED_WAR_PATH"
    ln -s "$WAR_PATH" "$RENAMED_WAR_PATH"
else
    RENAMED_WAR_PATH="$WAR_PATH"
fi

set +e

java weblogic.WLST << EOF
from java.util import *
from javax.management import *
import javax.management.Attribute
print "WLST:  Connecting..."
try:
    connect("weblogic", "weblogic1", "localhost:$PORT", timeout=$CONNECT_TIMEOUT)
    progress = deploy("$APPLICATION_NAME", "$RENAMED_WAR_PATH", timeout=$DEPLOY_TIMEOUT)
    if not progress.isCompleted():
        print "WLST: Deployment wasn't completed successfully. Failure: " + progress.getState()
        exit(exitcode=3)
except Exception, e:
    print "Exception occurred! " + str(e)
    exit(exitcode=4)

print "WLST: Deployment finished, exiting."
exit()
EOF

EXIT_CODE=$?
echo Deployment finished with $EXIT_CODE

exit $EXIT_CODE
