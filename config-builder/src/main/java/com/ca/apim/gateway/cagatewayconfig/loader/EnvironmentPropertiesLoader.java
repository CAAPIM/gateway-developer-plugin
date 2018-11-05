/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class EnvironmentPropertiesLoader extends PropertiesLoaderBase {

    private static final String ENV_PROPERTIES = "config/env.properties";

    @Inject
    EnvironmentPropertiesLoader(FileUtils fileUtils) {
        super(fileUtils);
    }

    @Override
    protected String getFilePath() {
        return ENV_PROPERTIES;
    }

    @Override
    protected void putToBundle(Bundle bundle, Map<String, String> properties) {
        bundle.putAllEnvironmentProperties(properties);
    }

    @Override
    public String getEntityType() {
        return "PROPERTY";
    }
}
