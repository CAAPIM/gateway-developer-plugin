package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
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

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createClusterProperty;
import static org.junit.jupiter.api.Assertions.*;

class ClusterPropertyFilterTest {

    @Test
    void filterNoEntities() {
        ClusterPropertyFilter filter = new ClusterPropertyFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<ClusterProperty> clusterProperties = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, clusterProperties.size());
    }

    @Test
    void filter() {
        ClusterPropertyFilter filter = new ClusterPropertyFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", ClusterProperty.class, "prop2", EntityTypes.CLUSTER_PROPERTY_TYPE), new Dependency("3", ClusterProperty.class, "prop3", EntityTypes.CLUSTER_PROPERTY_TYPE), new Dependency("6", ClusterProperty.class, "cluster.hostname", EntityTypes.CLUSTER_PROPERTY_TYPE)),
                        new Dependency("2", Policy.class, "policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", ClusterProperty.class, "prop4", EntityTypes.CLUSTER_PROPERTY_TYPE))));
        bundle.addEntity(createClusterProperty("prop1", "1", "1"));
        bundle.addEntity(createClusterProperty("prop2", "2", "2"));
        bundle.addEntity(createClusterProperty("prop3", "3", "3"));
        bundle.addEntity(createClusterProperty("prop4", "4", "4"));
        bundle.addEntity(createClusterProperty("cluster.hostname", "6", "6"));

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