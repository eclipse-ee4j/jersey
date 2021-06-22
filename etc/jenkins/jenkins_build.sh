#!/bin/bash -xe

mvn -V -U -B clean install glassfish-copyright:check -Dcopyright.quiet=false -Dsurefire.systemPropertiesFile=${WORKSPACE}/etc/jenkins/systemPropertiesFile