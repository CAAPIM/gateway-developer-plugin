/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity;
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

import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionFilterTest {

    @Test
    void filterNoEntities() {
        CassandraConnectionFilter filter = new CassandraConnectionFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<CassandraConnection> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        CassandraConnectionFilter filter = new CassandraConnectionFilter();

        Bundle filteredBundle = new Bundle();
        final Policy policy = TestUtils.createPolicy("my-policy", "1", "", "", null, "");
        filteredBundle.getPolicies().put(policy.getId(), policy);
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", CassandraConnection.class, "cassandra2", EntityTypes.CASSANDRA_CONNECTION_TYPE), new Dependency("3", CassandraConnection.class, "cassandra3", EntityTypes.CASSANDRA_CONNECTION_TYPE)),
                        new Dependency("2", Policy.class, "policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", CassandraConnection.class, "cassandra4", EntityTypes.CASSANDRA_CONNECTION_TYPE))));
        bundle.addEntity(TestUtils.createCassandraConnection("cassandra1", "1"));
        bundle.addEntity(TestUtils.createCassandraConnection("cassandra2", "2"));
        bundle.addEntity(TestUtils.createCassandraConnection("cassandra3", "3"));
        bundle.addEntity(TestUtils.createCassandraConnection("cassandra4", "4"));

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<CassandraConnection> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

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