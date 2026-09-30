#!/bin/bash -xe

export DEBUG=true

mvn -V -B -ntp -e  clean install
