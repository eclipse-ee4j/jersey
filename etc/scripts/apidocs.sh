#!/bin/bash

mvn -U -V -B clean install -Ppre-release -pl :apidocs -am -DskipTests