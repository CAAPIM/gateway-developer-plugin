/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class StaticPropertiesLoader extends PropertiesLoaderBase {

    private static final String STATIC_PROPERTIES = "config/static.properties";

    @Inject
    StaticPropertiesLoader(FileUtils fileUtils) {
        super(fileUtils);
    }

    @Override
    public String getFilePath() {
        return STATIC_PROPERTIES;
    }

    @Override
    protected void putToBundle(Bundle bundle, Map<String, String> properties) {
        bundle.putAllStaticProperties(properties);
    }
}
