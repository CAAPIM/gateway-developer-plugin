package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StoredPasswordFilterTest {
    @Test
    void filterNoEntities() {
        StoredPasswordFilter filter = new StoredPasswordFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<StoredPasswordEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        StoredPasswordFilter filter = new StoredPasswordFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", StoredPasswordEntity.class), new Dependency("3", StoredPasswordEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", StoredPasswordEntity.class))));
        bundle.addEntity(new StoredPasswordEntity.Builder().name("password1").id("1").properties(ImmutableMap.of("type", StoredPasswordEntity.Type.PASSWORD.getName())).build());
        bundle.addEntity(new StoredPasswordEntity.Builder().name("password2").id("2").properties(ImmutableMap.of("type", StoredPasswordEntity.Type.PASSWORD.getName())).build());
        bundle.addEntity(new StoredPasswordEntity.Builder().name("password3").id("3").properties(ImmutableMap.of("type", StoredPasswordEntity.Type.PEM_PRIVATE_KEY.getName())).build());
        bundle.addEntity(new StoredPasswordEntity.Builder().name("password4").id("4").properties(ImmutableMap.of("type", StoredPasswordEntity.Type.PASSWORD.getName())).build());


        List<StoredPasswordEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(1, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "password2".equals(c.getName())));
    }
}