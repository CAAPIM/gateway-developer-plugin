package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceFilterTest {
    @Test
    void filterNoEntities() {
        ServiceFilter filter = new ServiceFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<Service> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        ServiceFilter filter = new ServiceFilter();

        Bundle filteredBundle = new Bundle();
        Folder folder1 = createFolder("folder1", "1", null);
        Folder folder3 = createFolder("folder3", "3", folder1);
        Folder folder2 = createFolder("folder2", "2", folder3);
        Folder folder4 = createFolder("folder4", "4", folder1);
        filteredBundle.addEntity(folder1);
        filteredBundle.addEntity(folder2);
        filteredBundle.addEntity(folder3);
        filteredBundle.addEntity(folder4);
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "policy1", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", Service.class, "service2", EntityTypes.SERVICE_TYPE), new Dependency("3", Service.class, "service3", EntityTypes.SERVICE_TYPE)),
                        new Dependency("2", Policy.class, "policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", Service.class, "service4", EntityTypes.SERVICE_TYPE))));
        bundle.addEntity(createService("service1", "1", folder2, null, ""));
        bundle.addEntity(createService("service2", "2", createFolder("folder5", "5", null), null, ""));
        bundle.addEntity(createService("service3", "3", null, null, ""));
        bundle.addEntity(createService("service4", "4", folder1, null, ""));
        bundle.addEntity(createService("service5", "5", folder4, null, ""));

        List<Service> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "service1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "service4".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "service5".equals(c.getName())));
    }
}