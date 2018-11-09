/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class EntityLoaderUtils {

    @NotNull
    public static EntityLoader createEntityLoader(JsonTools jsonTools, IdGenerator idGenerator, GatewayEntityInfo entityInfo) {
        return new EntityLoaderBase<GatewayEntity>(jsonTools, idGenerator) {
            @Override
            public String getEntityType() {
                return entityInfo.getEnvironmentType();
            }

            @Override
            protected Class<GatewayEntity> getBeanClass() {
                return entityInfo.getEntityClass();
            }

            @Override
            protected String getFileName() {
                return entityInfo.getFileName();
            }

            @Override
            protected void putToBundle(Bundle bundle, @NotNull Map<String, GatewayEntity> entitiesMap) {
                bundle.getEntities(entityInfo.getEntityClass()).putAll(entitiesMap);
            }
        };
    }

    @NotNull
    public static PropertiesLoaderBase createPropertiesLoader(FileUtils fileUtils, IdGenerator idGenerator, GatewayEntityInfo entityInfo) {
        return new PropertiesLoaderBase(fileUtils, idGenerator) {

            @Override
            public String getEntityType() {
                return entityInfo.getEnvironmentType();
            }

            @Override
            protected String getFilePath() {
                return "config" + File.separator + entityInfo.getFileName() + ".properties";
            }

            @Override
            protected Class getEntityClass() {
                return entityInfo.getEntityClass();
            }
        };
    }
}
