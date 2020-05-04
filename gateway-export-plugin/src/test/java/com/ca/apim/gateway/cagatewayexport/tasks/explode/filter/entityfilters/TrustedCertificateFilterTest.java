package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
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

class TrustedCertificateFilterTest {
    @Test
    void filterNoEntities() {
        TrustedCertificateFilter filter = new TrustedCertificateFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<TrustedCert> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        TrustedCertificateFilter filter = new TrustedCertificateFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(TestUtils.createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", TrustedCert.class, "cert2", EntityTypes.TRUSTED_CERT_TYPE), new Dependency("3", TrustedCert.class, "cert3", EntityTypes.TRUSTED_CERT_TYPE)),
                        new Dependency("2", Policy.class, "my-policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", TrustedCert.class, "cert4", EntityTypes.TRUSTED_CERT_TYPE))));
        bundle.addEntity(new TrustedCert.Builder().name("cert1").id("1").build());
        bundle.addEntity(new TrustedCert.Builder().name("cert2").id("2").build());
        bundle.addEntity(new TrustedCert.Builder().name("cert3").id("3").build());
        bundle.addEntity(new TrustedCert.Builder().name("cert4").id("4").build());

        FilterConfiguration filterConfiguration = new FilterConfiguration();
        List<TrustedCert> filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert3".equals(c.getName())));

        filterConfiguration.getEntityFilters().put(filter.getFilterableEntityName(), new HashSet<>());
        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("cert1");
        filteredEntities = filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert3".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "cert1".equals(c.getName())));

        filterConfiguration.getEntityFilters().get(filter.getFilterableEntityName()).add("non-existing-entity");
        EntityFilterException entityFilterException = assertThrows(EntityFilterException.class, () -> filter.filter("/my/folder/path", filterConfiguration, bundle, filteredBundle));
        assertTrue(entityFilterException.getMessage().contains("non-existing-entity"));
    }
}