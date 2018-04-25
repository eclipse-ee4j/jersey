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
MW_HOME=$MW_HOME
DOMAIN=$DOMAIN
PORT=$PORT
SKIP_START_STOP=$SKIP_START_STOP
SKIP_STOP=$SKIP_STOP

STOP_TIME=20
KILL_TIME=5

if [ "$SKIP_START_STOP" = "true" -o "$SKIP_STOP" = "true" ]; then
    echo Stop skipped
    exit 0
fi

if [ "$MW_HOME" = "" -o "$DOMAIN" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

set +x
. $MW_HOME/wlserver/server/bin/setWLSEnv.sh
[ "$DEBUG" = "true" ] && set -x

cd $MW_HOME/$DOMAIN

set +x
sh bin/stopWebLogic.sh &
STOP_PID=$!

[ "$DEBUG" = "true" ] && set -x
for A in `seq $STOP_TIME`; do
    kill -0 $STOP_PID || break;
    sleep 1
done

set +e
kill -9 $STOP_PID

[ -f "$MW_HOME/$DOMAIN/wls.pid" ] && kill -9 `cat "$MW_HOME/$DOMAIN/wls.pid"`

for A in `seq $KILL_TIME`; do
    nc -z localhost $PORT || exit 0
    sleep 1
done

echo ERROR WLS server seems to not be shutdown!
exit 2
