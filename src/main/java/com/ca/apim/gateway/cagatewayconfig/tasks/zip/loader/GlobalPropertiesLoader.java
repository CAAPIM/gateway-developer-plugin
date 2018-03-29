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

public class GlobalPropertiesLoader implements EntityLoader {
    @Override
    public void load(Bundle bundle, File rootDir) {
        File globalPropertiesFile = new File(rootDir, "config/global.properties");
        if (globalPropertiesFile.exists()) {
            Properties properties = new Properties();
            try (FileInputStream inStream = new FileInputStream(globalPropertiesFile)) {
                properties.load(inStream);
            } catch (IOException e) {
                throw new BundleLoadException("Could not load global properties: " + e.getMessage(), e);
            }
            Map<String, String> map = new HashMap<>();
            properties.forEach((k, v) -> map.put(k.toString(), v.toString()));
            bundle.putAllGlobalProperties(map);
        }
    }
}
