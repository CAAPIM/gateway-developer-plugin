package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
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

class JDBCConnectionFilterTest {
    @Test
    void filterNoEntities() {
        JDBCConnectionFilter filter = new JDBCConnectionFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<JdbcConnection> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        JDBCConnectionFilter filter = new JDBCConnectionFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class), Arrays.asList(new Dependency("2", JdbcConnection.class), new Dependency("3", JdbcConnection.class)),
                        new Dependency("2", Policy.class), Collections.singletonList(new Dependency("4", JdbcConnection.class))));
        bundle.addEntity(new JdbcConnection.Builder().name("jdbc1").id("1").build());
        bundle.addEntity(new JdbcConnection.Builder().name("jdbc2").id("2").build());
        bundle.addEntity(new JdbcConnection.Builder().name("jdbc3").id("3").build());
        bundle.addEntity(new JdbcConnection.Builder().name("jdbc4").id("4").build());

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<JdbcConnection> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc3".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("jdbc1");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc3".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc1".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }
}