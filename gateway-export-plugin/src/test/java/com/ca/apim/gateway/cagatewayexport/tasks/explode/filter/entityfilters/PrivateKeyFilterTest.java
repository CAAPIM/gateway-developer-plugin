package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicy;
import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyFilterTest {
    @Test
    void filterNoEntities() {
        PrivateKeyFilter filter = new PrivateKeyFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<PrivateKey> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        PrivateKeyFilter filter = new PrivateKeyFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", PrivateKey.class, "SSL", EntityTypes.PRIVATE_KEY_TYPE), new Dependency("3", PrivateKey.class, "pk3", EntityTypes.PRIVATE_KEY_TYPE)),
                        new Dependency("2", Policy.class, "my-policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", PrivateKey.class, "pk4", EntityTypes.PRIVATE_KEY_TYPE))));
        bundle.addEntity(new PrivateKey.Builder().setAlias("pk1").setId("1").build());
        bundle.addEntity(new PrivateKey.Builder().setAlias("SSL").setId("2").build());
        bundle.addEntity(new PrivateKey.Builder().setAlias("pk3").setId("3").build());
        bundle.addEntity(new PrivateKey.Builder().setAlias("pk4").setId("4").build());
        bundle.addEntity(new PrivateKey.Builder().setAlias("ssl").setId("5").build());

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<PrivateKey> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(1, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "pk3".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("ssl");
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("pk1");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "pk3".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "pk1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "ssl".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }
}