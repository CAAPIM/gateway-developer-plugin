package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrustedCertificateFilterTest {
    @Test
    void filterNoEntities() {
        TrustedCertificateFilter filter = new TrustedCertificateFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<TrustedCertEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        TrustedCertificateFilter filter = new TrustedCertificateFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", TrustedCertEntity.class), new Dependency("3", TrustedCertEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", TrustedCertEntity.class))));
        bundle.addEntity(new TrustedCertEntity.Builder().name("cert1").id("1").build());
        bundle.addEntity(new TrustedCertEntity.Builder().name("cert2").id("2").build());
        bundle.addEntity(new TrustedCertEntity.Builder().name("cert3").id("3").build());
        bundle.addEntity(new TrustedCertEntity.Builder().name("cert4").id("4").build());

        List<TrustedCertEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert3".equals(c.getName())));
    }
}