package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClusterPropertyFilterTest {

    @Test
    void filterNoEntities() {
        ClusterPropertyFilter filter = new ClusterPropertyFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<ClusterProperty> clusterProperties = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, clusterProperties.size());
    }

    @Test
    void filter() {
        ClusterPropertyFilter filter = new ClusterPropertyFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", ClusterProperty.class), new Dependency("3", ClusterProperty.class), new Dependency("6", ClusterProperty.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", ClusterProperty.class))));
        bundle.addEntity(new ClusterProperty("prop1", "1", "1"));
        bundle.addEntity(new ClusterProperty("prop2", "2", "2"));
        bundle.addEntity(new ClusterProperty("prop3", "3", "3"));
        bundle.addEntity(new ClusterProperty("prop4", "4", "4"));
        bundle.addEntity(new ClusterProperty("cluster.hostname", "6", "6"));

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<ClusterProperty> clusterProperties = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, clusterProperties.size());
        assertTrue(clusterProperties.stream().anyMatch(c -> "prop2".equals(c.getName())));
        assertTrue(clusterProperties.stream().anyMatch(c -> "prop3".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("prop4");
        clusterProperties = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, clusterProperties.size());
        assertTrue(clusterProperties.stream().anyMatch(c -> "prop2".equals(c.getName())));
        assertTrue(clusterProperties.stream().anyMatch(c -> "prop3".equals(c.getName())));
        assertTrue(clusterProperties.stream().anyMatch(c -> "prop4".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("prop5");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("prop5"));
    }

}