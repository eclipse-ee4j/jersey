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
LOGFILE=$LOGFILE
ERROR_EXIT_CODE=$ERROR_EXIT_CODE

if [ "$LOGFILE" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

echo "Searching for java.lang.OutOfMemoryError exceptions in $LOGFILE"

set +e

if grep -nH 'java\.lang\.OutOfMemoryError' "$LOGFILE"; then
    echo "java.lang.OutOfMemoryError exceptions were found in logfile $LOGFILE!"
    exit $ERROR_EXIT_CODE
fi

echo "No java.lang.OutOfMemoryError exceptions were found in $LOGFILE"

exit 0
