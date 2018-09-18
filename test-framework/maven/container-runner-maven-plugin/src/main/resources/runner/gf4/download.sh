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
DIST_URL=$DIST_URL
DIST_TGT_LOCATION=$DIST_TGT_LOCATION
DIST_SUBDIR=$DIST_SUBDIR
DIST_DIR=$DIST_DIR

SKIP_START_STOP=$SKIP_START_STOP
DOWNLOAD_IF_EXISTS=$DOWNLOAD_IF_EXISTS
OVERWRITE=$OVERWRITE
all_proxy=$all_proxy


if [ "$SKIP_START_STOP" = "true" ]; then
    echo Download skipped
    exit 0
fi

if [ "$DIST_DIR" = "" -o "$DIST_TGT_LOCATION" = "" -o "$DIST_URL" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

if [ ! -f "$DIST_TGT_LOCATION" -o "$DOWNLOAD_IF_EXISTS" = "true" ]; then
    mkdir -p "$(dirname "$DIST_TGT_LOCATION")"
    curl -sS -o "$DIST_TGT_LOCATION" "$DIST_URL"
fi

if [ "$OVERWRITE" = "true" -o ! -d "$DIST_DIR"/"$DIST_SUBDIR" ]; then
    rm -rf "$DIST_DIR"/"$DIST_SUBDIR"
    mkdir -p "$DIST_DIR"
    unzip -o "$DIST_TGT_LOCATION" -d "$DIST_DIR"
    chmod -R 777 "$DIST_DIR"/"$DIST_SUBDIR"
fi
