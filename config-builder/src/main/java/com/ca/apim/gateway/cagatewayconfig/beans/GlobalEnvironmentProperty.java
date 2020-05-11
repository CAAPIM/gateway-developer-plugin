/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;

import java.io.File;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;

@Named("ENVIRONMENT_PROPERTY")
@ConfigurationFile(name = "global-env", type = PROPERTIES)
@EnvironmentType("PROPERTY")
public class GlobalEnvironmentProperty extends EnvironmentProperty {

    public GlobalEnvironmentProperty() { }

    public GlobalEnvironmentProperty(final String name, final String value) {
        super(name, value);
    }

    @Override
    public String getKey() {
        return "gateway." + getName();
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        String name = getName();
        int index = name.indexOf(".");
        setName(name.substring(index + 1));
    }
}
