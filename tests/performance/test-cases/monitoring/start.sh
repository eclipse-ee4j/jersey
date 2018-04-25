#!/bin/bash
#
# Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

LIBS=$(for l in `ls lib`; do echo -n lib/$l":";done)
LIBS=`echo $LIBS | sed -es'/:$//'`

java -server -Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m \
      -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods \
      -cp $LIBS \
      -Djava.net.preferIPv4Stack=true \
      -Dcom.sun.management.jmxremote \
      -Dcom.sun.management.jmxremote.port=11112 \
      -Dcom.sun.management.jmxremote.authenticate=false \
      -Dcom.sun.management.jmxremote.ssl=false \
      -Dcom.sun.management.jmxremote.local.only=false \
      org.glassfish.jersey.tests.performance.monitoring.JerseyApp $1
