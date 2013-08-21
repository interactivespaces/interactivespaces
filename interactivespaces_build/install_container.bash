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

${DO_CMD} rm ${CONTAINER_BOOTSTRAP}/*
${DO_CMD} rm ${CONTAINER_LIB_SYSTEM_JAVA}/*.jar
${DO_CMD} rm ${CONTAINER}/interactivespaces-launcher-*.jar

if [ ${CONTAINER_TYPE} == "controller" ]; then
  ${DO_CMD} rm ${CONTAINER_EXTRAS}/*
fi

if [ ${CONTAINER_TYPE} == "workbench" ]; then
  ${DO_CMD} rm -fR ${CONTAINER_TEMPLATES}/*
fi

${DO_CMD} cp ${STAGING}/bootstrap/* ${CONTAINER_BOOTSTRAP}
${DO_CMD} cp ${STAGING}/bin/* ${CONTAINER_BIN}
${DO_CMD} cp ${STAGING}/lib/system/java/*.jar ${CONTAINER_LIB_SYSTEM_JAVA}
${DO_CMD} cp ${STAGING}/interactivespaces-launcher-${VERSION}.jar ${CONTAINER}


if [ ${CONTAINER_TYPE} == "controller" ]; then
  ${DO_CMD} cp -R ${STAGING}/extras/* ${CONTAINER_EXTRAS}
fi

if [ ${CONTAINER_TYPE} == "workbench" ]; then
  ${DO_CMD} cp -R ${STAGING}/templates/* ${CONTAINER_TEMPLATES}
fi


