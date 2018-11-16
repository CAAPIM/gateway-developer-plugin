/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PropertiesEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesLoaderBaseTest {

    @Test
    void loadEntityWithoutConstructor() {
        PropertiesLoaderTest loader = new PropertiesLoaderTest(FileUtils.INSTANCE, new IdGenerator());
        assertThrows(ConfigLoadException.class, () -> loader.load(new Bundle(), "key", "value"));
    }

    private class PropertiesLoaderTest extends PropertiesLoaderBase<StaticPropertyEntity> {

        PropertiesLoaderTest(FileUtils fileUtils, IdGenerator idGenerator) {
            super(fileUtils, idGenerator);
        }

        @Override
        protected String getFilePath() {
            return "property";
        }

        @Override
        protected Class<StaticPropertyEntity> getEntityClass() {
            return StaticPropertyEntity.class;
        }

        @Override
        public String getEntityType() {
            return "StaticPropertyEntity";
        }
    }

    private class StaticPropertyEntity extends PropertiesEntity {

        @Override
        public String getKey() {
            return "key";
        }

        @Override
        public void setKey(String key) {
        }

        @Override
        public String getValue() {
            return "value";
        }

        @Override
        public void setValue(String value) {
        }
    }
}