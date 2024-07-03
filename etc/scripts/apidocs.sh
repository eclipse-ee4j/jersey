#!/bin/bash

mvn -U -V -B clean install -Ppre-release -Pstaging -pl :apidocs -am -DskipTests