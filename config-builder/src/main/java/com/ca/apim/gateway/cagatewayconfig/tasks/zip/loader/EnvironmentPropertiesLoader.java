/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import java.util.Map;

public class EnvironmentPropertiesLoader extends PropertiesLoaderBase {

    private static final String ENV_PROPERTIES = "config/env.properties";

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
}
