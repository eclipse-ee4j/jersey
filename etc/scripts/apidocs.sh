#!/bin/bash

mvn -U -V -B clean install javadoc:jar -Ppre-release -Pstaging -pl :apidocs -am -DskipTests