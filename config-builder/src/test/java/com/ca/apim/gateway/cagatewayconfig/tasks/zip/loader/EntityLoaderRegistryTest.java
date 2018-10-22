/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EntityLoaderRegistryTest {

    @Test
    void getLoader() {
        EntityLoaderRegistry registry = new EntityLoaderRegistry(
                Stream.of(new EntityLoader() {
                    @Override
                    public void load(Bundle bundle, File rootDir) {
                        //
                    }

                    @Override
                    public void load(Bundle bundle, String name, String value) {

                    }

                    @Override
                    public String getEntityType() {
                        return "ENTITY";
                    }
                }).collect(Collectors.toSet())
        );

        assertNotNull(registry.getEntityLoaders());
        assertNotNull(registry.getLoader("ENTITY"));
        assertNull(registry.getLoader("LOADER"));
    }
}