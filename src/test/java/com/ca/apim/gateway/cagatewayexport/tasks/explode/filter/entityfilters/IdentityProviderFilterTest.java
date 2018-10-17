package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.IdentityProvider;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IdentityProviderFilterTest {
    @Test
    void filterNoEntities() {
        IdentityProviderFilter filter = new IdentityProviderFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<IdentityProviderEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        IdentityProviderFilter filter = new IdentityProviderFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency(IdentityProviderEntity.INTERNAL_IDP_ID, IdentityProviderEntity.class), new Dependency("3", IdentityProviderEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", IdentityProviderEntity.class))));
        bundle.addEntity(new IdentityProviderEntity.Builder().name("idp1").id("1").build());
        bundle.addEntity(new IdentityProviderEntity.Builder().name("idp2").id(IdentityProviderEntity.INTERNAL_IDP_ID).build());
        bundle.addEntity(new IdentityProviderEntity.Builder().name("idp3").id("3").build());
        bundle.addEntity(new IdentityProviderEntity.Builder().name("idp4").id("4").build());


        List<IdentityProviderEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(1, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "idp3".equals(c.getName())));
    }
}