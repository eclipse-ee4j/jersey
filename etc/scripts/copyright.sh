#!/bin/bash

readonly CP_PATTERN='Copyright year is wrong'


[[ -n ${1} ]] && readonly LOG_FILE=${1} || readonly LOG_FILE='copyright.log'


echo ${LOG_FILE}

mvn -U -B  glassfish-copyright:copyright -Dcopyright.quiet=false | grep "${CP_PATTERN}" | tee ${LOG_FILE}

grep "${CP_PATTERN}"  ${LOG_FILE} || exit 0 && exit 1