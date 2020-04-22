/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JmsDestinationFilterTest {
    
    @Test
    void testFilterNoEntities() {
        JmsDestinationFilter filter = new JmsDestinationFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<JmsDestination> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void testFilter() {
        JmsDestinationFilter filter = new JmsDestinationFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", JmsDestination.class, "jms2", EntityTypes.JMS_DESTINATION_TYPE), new Dependency("3", JmsDestination.class, "jms3", EntityTypes.JMS_DESTINATION_TYPE)),
                        new Dependency("2", Policy.class, "my-policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", JmsDestination.class, "jms4", EntityTypes.JMS_DESTINATION_TYPE))));
        bundle.addEntity(TestUtils.createJmsDestination("jms1", "1"));
        bundle.addEntity(TestUtils.createJmsDestination("jms2", "2"));
        bundle.addEntity(TestUtils.createJmsDestination("jms3", "3"));
        bundle.addEntity(TestUtils.createJmsDestination("jms4", "4"));

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<JmsDestination> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "jms2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jms3".equals(c.getName())));
        
        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("jms1");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "jms2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jms3".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jms1".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }
}
