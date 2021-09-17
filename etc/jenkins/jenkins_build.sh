#!/bin/bash -xe

export DEBUG=true

mvn -V -U -B -e -Psnapshots clean install glassfish-copyright:check -Dcopyright.quiet=false