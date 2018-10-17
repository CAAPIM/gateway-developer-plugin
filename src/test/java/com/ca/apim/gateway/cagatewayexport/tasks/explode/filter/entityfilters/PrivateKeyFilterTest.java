package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PrivateKeyEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyFilterTest {
    @Test
    void filterNoEntities() {
        PrivateKeyFilter filter = new PrivateKeyFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<PrivateKeyEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        PrivateKeyFilter filter = new PrivateKeyFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", PrivateKeyEntity.class), new Dependency("3", PrivateKeyEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", PrivateKeyEntity.class))));
        bundle.addEntity(new PrivateKeyEntity.Builder().setAlias("pk1").setId("1").build());
        bundle.addEntity(new PrivateKeyEntity.Builder().setAlias("SSL").setId("2").build());
        bundle.addEntity(new PrivateKeyEntity.Builder().setAlias("pk3").setId("3").build());
        bundle.addEntity(new PrivateKeyEntity.Builder().setAlias("pk4").setId("4").build());
        bundle.addEntity(new PrivateKeyEntity.Builder().setAlias("ssl").setId("5").build());

        List<PrivateKeyEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(1, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "pk3".equals(c.getName())));
    }
}