#!/bin/sh

# check to make sure that there was only one gw7 file
gw7file=$(ls -1q /opt/docker/rc.d/*.gw7 | wc -l)
if [ "$gw7file" -gt "1" ]
then
    echo "Found multiple gw7 files. Cannot continue, this will cause unexpected results."
    exit 1
fi

# Call the apply-environment application
"$JAVA_HOME/bin/java" -classpath "/opt/docker/rc.d/apply-environment/*" com.ca.apim.gateway.cagatewayconfig.EnvironmentCreatorApplication