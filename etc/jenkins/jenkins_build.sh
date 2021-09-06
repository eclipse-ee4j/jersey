#!/bin/bash -xe

export DEBUG=true

mvn -V -U -B -e clean install glassfish-copyright:check -Dcopyright.quiet=false