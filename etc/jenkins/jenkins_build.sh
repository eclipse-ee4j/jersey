#!/bin/bash -xe

mvn -e -V -U -B clean install glassfish-copyright:check -Dcopyright.quiet=false -Dsurefire.systemPropertiesFile=${WORKSPACE}/etc/jenkins/systemPropertiesFile