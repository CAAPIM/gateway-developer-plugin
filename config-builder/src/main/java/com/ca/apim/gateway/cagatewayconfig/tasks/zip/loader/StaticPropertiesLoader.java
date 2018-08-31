/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StaticPropertiesLoader implements EntityLoader {
    @Override
    public void load(Bundle bundle, File rootDir) {
        File staticPropertiesFile = new File(rootDir, "config/static.properties");
        if (staticPropertiesFile.exists()) {
            Properties properties = new Properties();
            try (FileInputStream inStream = new FileInputStream(staticPropertiesFile)) {
                properties.load(inStream);
            } catch (IOException e) {
                throw new BundleLoadException("Could not load static properties: " + e.getMessage(), e);
            }
            Map<String, String> map = new HashMap<>();
            properties.forEach((k, v) -> map.put(k.toString(), v.toString()));
            bundle.putAllStaticProperties(map);
        }
    }
}
