#!/bin/sh

# Detemplatize all files matching the pattern $FILE_PATTERN_TO_TEMPLATIZE using values from environment variables
# starting with $DETEMPLATIZE_VAR_PREFIX.
#
# Template variables are only replaced in SetVariable Assertion where the Base64Expression element has the ENV_PARAM_NAME attribute
#
# For example if DETEMPLATIZE_VAR_PREFIX is 'ENV.' and the following environment variable are set:
# ENV.local.env.var=my_value
# ANOTHER_VAR=another_value
#
# When templatizing this file:
#        &lt;L7p:SetVariable&gt;
#            &lt;L7p:Base64Expression ENV_PARAM_NAME="ENV.local.env.var"/&gt;
#            &lt;L7p:VariableToSet stringValue="ENV.local.env.var"/&gt;
#        &lt;/L7p:SetVariable&gt;
#
# The result is:
#        &lt;L7p:SetVariable&gt;
#            &lt;L7p:Base64Expression stringValue="my_value"/&gt;
#            &lt;L7p:VariableToSet stringValue="ENV.local.env.var"/&gt;
#        &lt;/L7p:SetVariable&gt;

# Get the DETEMPLATIZE_VAR_PREFIX to use. Defaults to BUNDLE_TEMPLATE_
DETEMPLATIZE_VAR_PREFIX=${DETEMPLATIZE_VAR_PREFIX:-ENV.}
# This is the folder containing the files .bundle to templatize
mkdir -p /tmp/bundle
cp -r /opt/docker/rc.d/bundle/templatized /tmp/bundle/detemplatized
FILE_PATTERN_TO_TEMPLATIZE=${FILE_PATTERN_TO_TEMPLATIZE:-/tmp/bundle/detemplatized/*}
echo "Detemplatizing files matching: ${FILE_PATTERN_TO_TEMPLATIZE} var prefix is : ${DETEMPLATIZE_VAR_PREFIX}"

# Loop through all environment variables and match the ones starting with ${DETEMPLATIZE_VAR_PREFIX}
IFS=$'\n'       # make newlines the only separator
for env in `printenv`
do
    # Each line is an environment variable and value pair in the form of VAR=VALUE
    envVar=${env%%=*} # Get the environment var name. Everything before the '='
    echo "Processing environment variable:  ${envVar}"
    envValue=${env#*=} # Get the environment var name. Everything after the '='
    envValueEncoded=$(echo -n "${envValue}" | base64)

    # if the environment variable starts with the prefix then process this template variable
    if test ${envVar#${DETEMPLATIZE_VAR_PREFIX}} != "${envVar}" ; then
        templateVar=${envVar#${DETEMPLATIZE_VAR_PREFIX}} # Get the template var name. Everything after the '${DETEMPLATIZE_VAR_PREFIX}'

        # For each file that should be de-templatized
        IFS=$' ' # make the file separator the space character
        for file in ${FILE_PATTERN_TO_TEMPLATIZE}
        do
            if [[ -e ${file} ]] && [[ -w ${file} ]] && [[ -f ${file} ]] ; then # makes sure the file exists and is writable
                echo "Detemplatizing variable '${templateVar}' with value '${envValue}' in file: ${file}"
                # replace all instances of $#{var}# with the value of $var
                sed -i.bak "s/L7p:Base64Expression ENV_PARAM_NAME=\"ENV.${templateVar}\"/L7p:Base64Expression stringValue=\"${envValueEncoded}\"/g" ${file}
                sed -i.bak "s/l7:StringValue>SERVICE_PROPERTY_ENV.${templateVar}</l7:StringValue>${envValue}</g" ${file}
                sed -i.bak "s/l7:Value env=\"true\">ENV.${templateVar}</l7:Value>${envValue}</g" ${file}
                rm -f ${FILE_PATTERN_TO_TEMPLATIZE}.bak
            fi
        done
    fi
done

# For each file that should be de-templatized
IFS=$' ' # make the file separator the space character
for file in ${FILE_PATTERN_TO_TEMPLATIZE}
do
    if grep -q "L7p:Base64Expression ENV_PARAM_NAME" "${file}"; then
        grep "L7p:Base64Expression ENV_PARAM_NAME" "${file}"
        exit "Need to provide additional environment Variables"
    fi
    if grep -q "l7:StringValue>SERVICE_PROPERTY_ENV." "${file}"; then
        grep "l7:StringValue>SERVICE_PROPERTY_ENV." "${file}"
        exit "Need to provide additional environment Variables"
    fi
    if grep -q "l7:Value env=\"true\">ENV." "${file}"; then
        grep "l7:Value env=\"true\">ENV." "${file}"
        exit "Need to provide additional environment Variables"
    fi
done

cp -r /tmp/bundle/detemplatized/* /opt/SecureSpan/Gateway/node/default/etc/bootstrap/bundle/
rm -rf /tmp/bundle/detemplatized