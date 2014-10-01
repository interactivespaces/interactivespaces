# Copyright (C) 2013 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

VERSION=$1
CONTAINER_TYPE=$2
CONTAINER=$3
STAGING=$4

# How to execute a command
DO_CMD=
#DO_CMD=echo

echo Installing IS version ${VERSION} into container ${CONTAINER} of type ${CONTAINER_TYPE} from ${STAGING}

CONTAINER_BIN=${CONTAINER}/bin
CONTAINER_BOOTSTRAP=${CONTAINER}/bootstrap
CONTAINER_LIB_SYSTEM_JAVA=${CONTAINER}/lib/system/java
CONTAINER_EXTRAS=${CONTAINER}/extras
CONTAINER_TEMPLATES=${CONTAINER}/templates

${DO_CMD} rm  ${CONTAINER_BOOTSTRAP}/*
${DO_CMD} rm  ${CONTAINER_LIB_SYSTEM_JAVA}/*.jar
${DO_CMD} rm  ${CONTAINER}/interactivespaces-launcher-*.jar

if [ ${CONTAINER_TYPE} == "controller" ]; then
  ${DO_CMD} rm -fR ${CONTAINER_EXTRAS}/*
fi

if [ ${CONTAINER_TYPE} == "workbench" ]; then
  ${DO_CMD} rm -fR ${CONTAINER_TEMPLATES}/*
fi

${DO_CMD} cp ${STAGING}/bootstrap/* ${CONTAINER_BOOTSTRAP}
${DO_CMD} cp ${STAGING}/bin/* ${CONTAINER_BIN}
${DO_CMD} cp ${STAGING}/lib/system/java/delegations.conf ${CONTAINER_LIB_SYSTEM_JAVA}
${DO_CMD} cp ${STAGING}/lib/system/java/*.jar ${CONTAINER_LIB_SYSTEM_JAVA}
${DO_CMD} cp ${STAGING}/interactivespaces-launcher-${VERSION}.jar ${CONTAINER}


if [ ${CONTAINER_TYPE} == "controller" ]; then
  ${DO_CMD} cp -R ${STAGING}/extras/* ${CONTAINER_EXTRAS}
fi

if [ ${CONTAINER_TYPE} == "workbench" ]; then
  ${DO_CMD} cp -R ${STAGING}/templates/* ${CONTAINER_TEMPLATES}
  mkdir -p ${CONTAINER_EXTRAS}
  ${DO_CMD} cp -R ${STAGING}/extras/* ${CONTAINER_EXTRAS}
fi


