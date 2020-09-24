#!/bin/sh

# check to make sure that there was only one gw7 file
gw7file=$(ls -1q /opt/docker/rc.d/*.gw7 | wc -l)
if [ "$gw7file" -gt "1" ]
then
    echo "Found multiple gw7 files. Cannot continue, this will cause unexpected results."
    exit 1
fi

# Allow debugging of the env creator application
DEBUG_OPTS=""
if [ "$ENV_DEBUG" = "true" ]
then
    DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=${ENV_DEBUG_PORT}"
fi

# Call the apply-environment application
"$JAVA_HOME/bin/java" ${DEBUG_OPTS} -classpath "/opt/docker/rc.d/apply-environment/*" com.ca.apim.gateway.cagatewayconfig.EnvironmentCreatorApplication