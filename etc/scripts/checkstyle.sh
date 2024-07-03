#!/bin/bash

mvn -V -U -B  -Pstaging checkstyle:checkstyle-aggregate -Dcheckstyle.output.format="plain" -Dcheckstyle.output.file=checkstyle.log