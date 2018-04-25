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

#
# Environment setup:
#
# GIT_FETCH_COMMAND - specify GIT command to get sources to test, e.g. git checkout master && git pull
# WEBLOGIC_OUI_URL - URL to download WLS installation jar from
#
# STATUS_DIR=$HOME/.hudson-jersey3-performance-test
# mkdir -p $STATUS_DIR
# source jersey-performance-test-common.sh
# test machine sets: cfg#, group, server, clients
# createMachineFiles 1 1 server1 client1a client1b
# createMachineFiles 1 2 server2 client2a client2b
# MEASUREMENT_DATA=~/MEASUREMENT_DATA
#

LOGS_DIR=$WORKSPACE/logs

SERVER_PORT=7001

function singleTest() {
  echo "================================================================================="
  echo "===== SINGLE TEST RUN, SERVER=$SERVER_MACHINE, loader=$ab_cmdline, app=$app, JMX_URI=$JMX_URI, group_id=$group_id ====="
  echo "================================================================================="

  echo "########### Start WLS server"
  ssh -n jerseyrobot@${SERVER_MACHINE} '(cd workspace/jersey/tests/performance/runners/jersey-wls-runner; ./start.sh)' &

  echo "########### waiting for WLS server start"
  while [ ! `wget -q --server-response --no-proxy http://${SERVER_MACHINE}:7001 2>&1 | awk '/^  HTTP/{print $2}'` ]; do
    sleep 5
    echo "# ${SERVER_MACHINE}"
  done

  echo "########### going to deploy/start the test app $app"
  ssh -n jerseyrobot@${SERVER_MACHINE} '(cd workspace/jersey/tests/performance/runners/jersey-wls-runner; ./deploy.sh $PWD/../../test-cases/'$app'/target/runner/'$app'.war)'

  waitForGroupStatus $actual_runner $group_id "open"

  for client_machine in ${CLIENT_LIST[*]}; do
    echo "########### going to start load generator at $client_machine"
    (sleep $WAIT_FOR_APP_STARTUP_SEC; ssh -n jerseyrobot@$client_machine "nohup $ab_cmdline" & ) &
  done

  echo "########### waiting $WARM_UP_SECONDS sec to warm up server"
  sleep $WARM_UP_SECONDS

  echo "########### warm up finished, terminating ab clients..."
  for client_machine in ${CLIENT_LIST[*]}; do
    echo -n "########### warm up finished, going to stop load generator at $client_machine..."
    ssh -n jerseyrobot@$client_machine 'if ! test -e `ps h o pid -Cwrk`; then kill -s INT `ps h o pid -Cwrk` ; fi'
    echo " done."
  done

  waitForGroupStatus $actual_runner $group_id "lock"

  for client_machine in ${CLIENT_LIST[*]}; do
    echo "########### going to start load generator at $client_machine again"
    (ssh -n jerseyrobot@$client_machine "nohup $ab_cmdline" & ) &
  done

  echo "########### waiting before start capturing jmx data"
  sleep $WAIT_FOR_APP_RUNNING_SEC

  echo "########### starting jmx client to capture data"
  if ! java -cp jmxclient.jar org.glassfish.jersey.tests.performance.jmxclient.Main $JMX_URI "$mbean" OneMinuteRate $SAMPLES $filename; then
    echo "########### ERROR WHEN PROCESSING LINE#${LINE_NUMBER}, test-case: ${app}, mbean: ${mbean}, filename: ${filename}!"
  fi

  echo "########### jmx client finished, terminating ab clients..."
  for client_machine in ${CLIENT_LIST[*]}; do
    echo -n "########### going to stop load generator at $client_machine..."
    ssh -n jerseyrobot@$client_machine 'if ! test -e `ps h o pid -Cwrk`; then kill -s INT `ps h o pid -Cwrk` ; fi'
    echo " done."
  done

  echo "########### terminating test app..."
  ssh jerseyrobot@${SERVER_MACHINE} '(cd workspace/jersey/tests/performance/runners/jersey-wls-runner && ./stop.sh)'

  echo "########### copy server and domain logs to hudson slave..."
  just_filename=`echo ${filename} | sed -e 's/\.[^.]*$//'`
  scp jerseyrobot@${SERVER_MACHINE}:workspace/jersey/tests/performance/runners/jersey-wls-runner/target/server.log $LOGS_DIR/${just_filename}-server.log
  scp jerseyrobot@${SERVER_MACHINE}:workspace/jersey/tests/performance/runners/jersey-wls-runner/target/domain.log $LOGS_DIR/${just_filename}-domain.log

  cleanupServer $SERVER_MACHINE

  releaseRunnerAndGroup $actual_runner $group_id
}

#
# test process start
#

mkdir -p $LOGS_DIR
rm -f $LOGS_DIR/*

removeOldCapturedData

retrieveJmxClient

prepareClients

buildTestAppOnServers

echo "########### Prepare war files of all test case applications"
for SERVER_MACHINE in ${SERVER_LIST[@]}; do
  for app in ${APP_LIST[*]}; do
    ssh jerseyrobot@${SERVER_MACHINE} '(cd workspace/jersey/tests/performance/test-cases/'$app'; mkdir -p target/runner; cp target/*.war target/runner/'$app'.war)'
  done
done

echo "########### Install WLS server"
for SERVER_MACHINE in ${SERVER_LIST[@]}; do
  ssh -n jerseyrobot@${SERVER_MACHINE} '(cd workspace/jersey/tests/performance/runners/jersey-wls-runner; ./install.sh '$WEBLOGIC_OUI_URL')' &
done

wait

cleanupServers

testLoop

waitForTerminator
