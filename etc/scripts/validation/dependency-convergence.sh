#!/bin/bash

readonly VALIDATION_DEPENDENCIES_MATCH='<!-- Insert dependencies here -->'
readonly VALIDATION_POM=validation.pom.xml
readonly POM_TEMPLATE_NAME=pom.template.xml
readonly TEMP_FILE=modules.xml

readonly MVN_CLI='org.apache.maven.plugins:maven-enforcer-plugin:enforce -Denforcer.rules=dependencyConvergence'

readonly DEPENDENCY_GENERATION_PATTERN='<dependency><groupId>${project.groupId}</groupId><artifactId>${project.artifactId}</artifactId><version>${jersey.version}</version></dependency>'

# Path to this script
[ -h "${0}" ] && readonly SCRIPT_PATH="$(readlink "${0}")" || readonly SCRIPT_PATH="${0}"

readonly CURRENT_LOCATION=$(dirname -- "${SCRIPT_PATH}")
readonly WS_DIR=$(cd ${CURRENT_LOCATION}; cd '../../..' ; pwd -P)

#Prepare pom.xml from template

cp -a ${CURRENT_LOCATION}/${POM_TEMPLATE_NAME} ${CURRENT_LOCATION}/${VALIDATION_POM}

export JERSEY_VERSION=$(mvn exec:exec -Dexec.executable='echo' -Dexec.args='${project.version}' -f ${WS_DIR}/pom.xml -q -pl org.glassfish.jersey:project)

echo "Validating convergences for Jersey "$JERSEY_VERSION
echo '****************************************************'
echo `mvn -v`
echo '****************************************************'

#get list of modules to b validated
mvn -f ${WS_DIR}/pom.xml -Dexec.executable='echo' \
 -Dtests.excluded \
 -Dexec.args=${DEPENDENCY_GENERATION_PATTERN} \
 ${MVN_ARGS} \
 ${EXCLUDE_ARGS} \
 -pl '!:helloworld-benchmark' exec:exec -q > ${CURRENT_LOCATION}/${TEMP_FILE}

#add the list of modules to the prepared pom.xml
sed -e "/${VALIDATION_DEPENDENCIES_MATCH}/ {" -e "r ${CURRENT_LOCATION}/${TEMP_FILE}" -e 'd' -e '}'  -i ${CURRENT_LOCATION}/${VALIDATION_POM}

#run validation
mvn ${MVN_ARGS} ${MVN_CLI} -f ${CURRENT_LOCATION}/${VALIDATION_POM} -Djersey.version=${JERSEY_VERSION}

#save exit status
export MAVEN_BUILD_RESULT=$?

#clean the mess
rm ${CURRENT_LOCATION}/${TEMP_FILE}

#exit with saved exit stateus
exit $MAVEN_BUILD_RESULT
