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

WEBLOGIC_URL=$1

TARGET=$PWD/target
OPT=$TARGET/opt

rm -rf $TARGET
mkdir -p $OPT

cd $TARGET
rm -f wls.jar
wget --no-proxy $WEBLOGIC_URL -O wls.jar

cd $OPT
java -jar $TARGET/wls.jar
cd wls*

MW_HOME=`pwd`
echo $MW_HOME > $TARGET/mw_home.txt

. $MW_HOME/wlserver/server/bin/setWLSEnv.sh

TEST_DOMAIN=$MW_HOME/hudson_test_domain

mkdir -p $TEST_DOMAIN
cd $TEST_DOMAIN

