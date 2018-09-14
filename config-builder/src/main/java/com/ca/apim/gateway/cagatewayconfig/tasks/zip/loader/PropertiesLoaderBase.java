/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class PropertiesLoaderBase implements EntityLoader {

    private FileUtils fileUtils;

    protected PropertiesLoaderBase(final FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        File propertiesFile = new File(rootDir, this.getFilePath());
        if (propertiesFile.exists()) {
            Properties properties = new Properties();
            try (InputStream inStream = fileUtils.getInputStream(propertiesFile)) {
                properties.load(inStream);
            } catch (IOException e) {
                throw new BundleLoadException("Could not load properties file (" + propertiesFile + "): " + e.getMessage(), e);
            }
            Map<String, String> map = new HashMap<>();
            properties.forEach((k, v) -> map.put(k.toString(), v.toString()));
            this.putToBundle(bundle, map);
        }
    }

    /**
     * @return the file path for this loader
     */
    protected abstract String getFilePath();

    /**
     * Put the loaded properties into the bundle.
     *
     * @param properties map of properties
     */
    protected abstract void putToBundle(Bundle bundle, Map<String, String> properties);
}
