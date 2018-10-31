/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
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

class CassandraConnectionFilterTest {

    @Test
    void filterNoEntities() {
        CassandraConnectionFilter filter = new CassandraConnectionFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<CassandraConnectionEntity> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        CassandraConnectionFilter filter = new CassandraConnectionFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", CassandraConnectionEntity.class), new Dependency("3", CassandraConnectionEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", CassandraConnectionEntity.class))));
        bundle.addEntity(new CassandraConnectionEntity.Builder().name("cassandra1").id("1").build());
        bundle.addEntity(new CassandraConnectionEntity.Builder().name("cassandra2").id("2").build());
        bundle.addEntity(new CassandraConnectionEntity.Builder().name("cassandra3").id("3").build());
        bundle.addEntity(new CassandraConnectionEntity.Builder().name("cassandra4").id("4").build());

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<CassandraConnectionEntity> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "cassandra2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cassandra3".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("cassandra1");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "cassandra2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cassandra3".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cassandra1".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }

}