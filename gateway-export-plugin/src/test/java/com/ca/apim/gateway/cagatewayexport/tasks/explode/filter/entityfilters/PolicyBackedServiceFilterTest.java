package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicy;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicyBackedService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyBackedServiceFilterTest {
    @Test
    void filterNoEntities() {
        PolicyBackedServiceFilter filter = new PolicyBackedServiceFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<PolicyBackedService> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        PolicyBackedServiceFilter filter = new PolicyBackedServiceFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(createPolicy("policy1", "1", "", null, null, ""));
        filteredBundle.addEntity(createPolicy("policy2", "2", "", null, null, ""));
        filteredBundle.addEntity(createPolicy("policy3", "3", "", null, null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class), Arrays.asList(new Dependency("2", PolicyBackedService.class), new Dependency("3", PolicyBackedService.class)),
                        new Dependency("3", Policy.class), Arrays.asList(new Dependency("2", PolicyBackedService.class), new Dependency("3", PolicyBackedService.class)),
                        new Dependency("2", Policy.class), Collections.singletonList(new Dependency("4", PolicyBackedService.class))));
        bundle.addEntity(createPolicyBackedService("pbs1", "1", "", ImmutableMap.of("p1", "1", "p2", "2")));
        bundle.addEntity(createPolicyBackedService("pbs2", "2", "", Collections.emptyMap()));
        bundle.addEntity(createPolicyBackedService("pbs3", "3", "", ImmutableMap.of("p2", "2")));
        bundle.addEntity(createPolicyBackedService("pbs4", "4", "", Collections.emptyMap()));


        List<PolicyBackedService> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "pbs1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "pbs3".equals(c.getName())));
    }
}