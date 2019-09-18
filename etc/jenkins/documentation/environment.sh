#!/bin/bash

#
# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

#ALL main input parameters are collected here. Ideally these shall be set as input parameters for
#Jenkins job but if for some reason the script is being run from local host this file shall be used
#and mdified accordingly to provide correct intput data for the main script

export RELEASE_VERSION='2.29.1'
export WEBSITE_URL='https://github.com/eclipse-ee4j/jersey.github.io'
export BRANCH_SPECIFIER='master'
export UPDATE_LATEST=true
export WEBSITE_SOURCE_REPO='https://github.com/eclipse-ee4j/jersey-web'
export WORKSPACE=`pwd`
export DRY_RUN=false

export USER_NAME='' #ideally shall be taken from job's credentials but for local run it's user name
export USER_TOKEN='' #ideally shall be taken from job's credentials but for local run it's user password or token
