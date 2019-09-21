#!/bin/bash
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

TARGET=$PWD/target
MW_HOME=`cat $TARGET/mw_home.txt`
TEST_DOMAIN=$MW_HOME/hudson_test_domain
DOMAIN_NAME=HudsonTestDomain
SERVER_NAME=HudsonTestServer
PID_FILE=$TARGET/wls.pid

echo $TEST_DOMAIN > $TARGET/test_domain.txt

cd $MW_HOME
. $MW_HOME/wlserver/server/bin/setWLSEnv.sh

rm -rf $TEST_DOMAIN
mkdir -p $TEST_DOMAIN
cd $TEST_DOMAIN

rm -f $TARGET/autodeploy
ln -s $TEST_DOMAIN/autodeploy $TARGET/autodeploy

rm -f $TARGET/server.log
rm -f $TARGET/domain.log
ln -s $TEST_DOMAIN/servers/$SERVER_NAME/logs/$SERVER_NAME.log $TARGET/server.log
ln -s $TEST_DOMAIN/servers/$SERVER_NAME/logs/$DOMAIN_NAME.log $TARGET/domain.log

JAVA_OPTIONS="-javaagent:$HOME/jersey-perftest-agent.jar"

yes | nohup java -server \
      -Xms1024m \
      -Xmx1024m \
      -XX:MaxPermSize=256m \
      -Dweblogic.Domain=$DOMAIN_NAME \
      -Dweblogic.Name=$SERVER_NAME \
      -Dweblogic.management.username=weblogic \
      -Dweblogic.management.password=weblogic1 \
      -Dweblogic.ListenPort=7001 \
      -Djava.security.egd=file:/dev/./urandom \
      -Djava.net.preferIPv4Stack=true \
      -Dcom.sun.management.jmxremote \
      -Dcom.sun.management.jmxremote.port=11112 \
      -Dcom.sun.management.jmxremote.authenticate=false \
      -Dcom.sun.management.jmxremote.ssl=false \
      -Dcom.sun.management.jmxremote.local.only=false \
      $JAVA_OPTIONS \
      weblogic.Server &

echo $! > $PID_FILE

# wait for server to start
echo "******** WAITING FOR SERVER TO START"
while [ ! `wget -q --server-response --no-proxy http://localhost:7001 2>&1 | awk '/^  HTTP/{print $2}'` ]; do
  sleep 5
  echo "*"
done
echo "******** SERVER IS READY"
