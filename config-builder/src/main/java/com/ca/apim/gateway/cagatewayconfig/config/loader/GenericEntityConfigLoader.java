/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;

import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.findConfigFileOrDir;
import static java.util.Arrays.stream;
import static org.apache.commons.io.FilenameUtils.getBaseName;

/**
 * Loader for the generic entity configuration files, stored separately from generic entity main data.
 */
@Singleton
public class GenericEntityConfigLoader implements EntityLoader {

    @Override
    public void load(Bundle bundle, File rootDir) {
        final File genericEntitiesDir = findConfigFileOrDir(rootDir, "genericEntities");
        if (genericEntitiesDir == null || !genericEntitiesDir.exists()) {
            // no generic entities config dir
            return;
        }

        final File[] genericEntitySubfolders = genericEntitiesDir.listFiles(File::isDirectory);
        if (genericEntitySubfolders == null || genericEntitySubfolders.length == 0) {
            // no valid subfolder into generic entity folder
            return;
        }

        final Map<String, File> map = new HashMap<>();
        stream(genericEntitySubfolders).forEach(folder -> processFolder(folder, map));
        bundle.putAllGenericEntityConfigurations(map);
    }

    private void processFolder(File folder, Map<String, File> map) {
        final String[] files = folder.list();
        if (files == null || files.length == 0) {
            // no files into generic entity dir
            return;
        }

        stream(files).forEach(fileName -> map.put(GenericEntity.createKey(folder.getName(), getBaseName(fileName)), new File(folder, fileName)));
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual generic entity config");
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual generic entity config");
    }

    @Override
    public String getEntityType() {
        return "GENERIC_ENTITY_CONFIG";
    }
}
