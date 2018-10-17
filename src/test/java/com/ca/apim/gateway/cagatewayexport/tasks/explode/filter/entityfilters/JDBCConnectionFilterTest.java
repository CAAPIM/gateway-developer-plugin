package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JDBCConnectionFilterTest {
    @Test
    void filterNoEntities() {
        JDBCConnectionFilter filter = new JDBCConnectionFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<JdbcConnectionEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        JDBCConnectionFilter filter = new JDBCConnectionFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", JdbcConnectionEntity.class), new Dependency("3", JdbcConnectionEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", JdbcConnectionEntity.class))));
        bundle.addEntity(new JdbcConnectionEntity.Builder().name("jdbc1").id("1").build());
        bundle.addEntity(new JdbcConnectionEntity.Builder().name("jdbc2").id("2").build());
        bundle.addEntity(new JdbcConnectionEntity.Builder().name("jdbc3").id("3").build());
        bundle.addEntity(new JdbcConnectionEntity.Builder().name("jdbc4").id("4").build());

        List<JdbcConnectionEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "jdbc3".equals(c.getName())));
    }
}