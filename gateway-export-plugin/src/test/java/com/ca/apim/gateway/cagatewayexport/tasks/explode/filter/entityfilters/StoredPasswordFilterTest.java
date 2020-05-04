package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
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

class StoredPasswordFilterTest {
    @Test
    void filterNoEntities() {
        StoredPasswordFilter filter = new StoredPasswordFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<StoredPassword> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        StoredPasswordFilter filter = new StoredPasswordFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", StoredPassword.class, "password2", EntityTypes.STORED_PASSWORD_TYPE), new Dependency("3", StoredPassword.class, "password3", EntityTypes.STORED_PASSWORD_TYPE)),
                        new Dependency("2", Policy.class, "my-policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", StoredPassword.class, "password4", EntityTypes.STORED_PASSWORD_TYPE))));
        bundle.addEntity(new StoredPassword.Builder().name("password1").id("1").properties(ImmutableMap.of("type", StoredPassword.Type.PASSWORD.getName())).build());
        bundle.addEntity(new StoredPassword.Builder().name("password2").id("2").properties(ImmutableMap.of("type", StoredPassword.Type.PASSWORD.getName())).build());
        bundle.addEntity(new StoredPassword.Builder().name("password3").id("3").properties(ImmutableMap.of("type", StoredPassword.Type.PEM_PRIVATE_KEY.getName())).build());
        bundle.addEntity(new StoredPassword.Builder().name("password4").id("4").properties(ImmutableMap.of("type", StoredPassword.Type.PASSWORD.getName())).build());


        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<StoredPassword> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(1, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "password2".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("password4");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "password2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "password4".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }
}