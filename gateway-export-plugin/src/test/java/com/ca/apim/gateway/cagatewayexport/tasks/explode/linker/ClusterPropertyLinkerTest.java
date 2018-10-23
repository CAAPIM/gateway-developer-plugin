/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterPropertyLinkerTest {

    @Test
    void link() {
        ClusterPropertyLinker clusterPropertyLinker = new ClusterPropertyLinker();

        Bundle bundle = new Bundle();
        bundle.addEntity(new ClusterProperty("my.name", "my-value", "1"));
        bundle.addEntity(new ClusterProperty("ENV.my.name", "my-value", "2"));
        bundle.addEntity(new ClusterProperty("another", "my-value", "3"));
        bundle.addEntity(new ClusterProperty("ENV.hello", "my-value", "4"));

        clusterPropertyLinker.link(bundle, null);

        assertEquals(0, bundle.getEntities(ClusterProperty.class).size());
        assertEquals(4, bundle.getEntities(EnvironmentProperty.class).size());

        assertTrue(bundle.getEntities(EnvironmentProperty.class).containsKey("GLOBAL:my.name"));
        assertEquals("my.name", bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:my.name").getName());
        assertEquals(EnvironmentProperty.Type.GLOBAL, bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:my.name").getType());
        assertTrue(bundle.getEntities(EnvironmentProperty.class).containsKey("GLOBAL:ENV.hello"));
        assertEquals("ENV.hello", bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:ENV.hello").getName());

    }
}