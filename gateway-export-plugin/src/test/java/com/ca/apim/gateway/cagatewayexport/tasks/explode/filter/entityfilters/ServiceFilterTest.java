package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceFilterTest {
    @Test
    void filterNoEntities() {
        ServiceFilter filter = new ServiceFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<ServiceEntity> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        ServiceFilter filter = new ServiceFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new Folder("folder1", "1", null));
        filteredBundle.addEntity(new Folder("folder2", "2", "3"));
        filteredBundle.addEntity(new Folder("folder3", "3", "1"));
        filteredBundle.addEntity(new Folder("folder4", "4", "1"));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", ServiceEntity.class), new Dependency("3", ServiceEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", ServiceEntity.class))));
        bundle.addEntity(new ServiceEntity("service1", "1", "2", null, ""));
        bundle.addEntity(new ServiceEntity("service2", "2", "5", null, ""));
        bundle.addEntity(new ServiceEntity("service3", "3", null, null, ""));
        bundle.addEntity(new ServiceEntity("service4", "4", "1", null, ""));
        bundle.addEntity(new ServiceEntity("service5", "5", "4", null, ""));

        List<ServiceEntity> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "service1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "service4".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "service5".equals(c.getName())));
    }
}