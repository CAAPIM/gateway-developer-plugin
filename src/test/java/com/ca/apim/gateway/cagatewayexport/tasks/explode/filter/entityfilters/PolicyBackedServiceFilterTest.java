package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PolicyBackedServiceFilterTest {
    @Test
    void filterNoEntities() {
        PolicyBackedServiceFilter filter = new PolicyBackedServiceFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<PolicyBackedServiceEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        PolicyBackedServiceFilter filter = new PolicyBackedServiceFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("policy1", "1", "", null, null, ""));
        filteredBundle.addEntity(new PolicyEntity("policy2", "2", "", null, null, ""));
        filteredBundle.addEntity(new PolicyEntity("policy3", "3", "", null, null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", PolicyBackedServiceEntity.class), new Dependency("3", PolicyBackedServiceEntity.class)),
                        new Dependency("3", PolicyEntity.class), Arrays.asList(new Dependency("2", PolicyBackedServiceEntity.class), new Dependency("3", PolicyBackedServiceEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", PolicyBackedServiceEntity.class))));
        bundle.addEntity(new PolicyBackedServiceEntity("pbs1", "1", "", ImmutableMap.of("p1", "1", "p2", "2")));
        bundle.addEntity(new PolicyBackedServiceEntity("pbs2", "2", "", Collections.emptyMap()));
        bundle.addEntity(new PolicyBackedServiceEntity("pbs3", "3", "", ImmutableMap.of("p2", "2")));
        bundle.addEntity(new PolicyBackedServiceEntity("pbs4", "4", "", Collections.emptyMap()));


        List<PolicyBackedServiceEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "pbs1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "pbs3".equals(c.getName())));
    }
}