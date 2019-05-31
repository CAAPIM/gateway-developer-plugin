/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;

@Named("SERVICE_ENVIRONMENT_PROPERTY")
@ConfigurationFile(name = "service-env", type = PROPERTIES)
@EnvironmentType("SERVICE_PROPERTY")
public class ServiceEnvironmentProperty extends EnvironmentProperty {

    public ServiceEnvironmentProperty() { }

    public ServiceEnvironmentProperty(final String name, final String value) {
        super(name, value);
    }

    @Override
    public String getKey() {
        return "service.property." + getName();
    }
}