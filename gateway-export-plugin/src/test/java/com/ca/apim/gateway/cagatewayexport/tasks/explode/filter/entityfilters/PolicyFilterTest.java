package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyFilterTest {
    @Test
    void filterNoEntities() {
        PolicyFilter filter = new PolicyFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<Policy> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        PolicyFilter filter = new PolicyFilter();

        Bundle filteredBundle = new Bundle();
        Folder folder1 = createFolder("folder1", "1", null);
        Folder folder3 = createFolder("folder3", "3", folder1);
        filteredBundle.addEntity(folder1);
        filteredBundle.addEntity(createFolder("folder2", "2", folder3));
        filteredBundle.addEntity(folder3);
        filteredBundle.addEntity(createFolder("folder4", "4", folder1));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "policy1", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", Policy.class, "policy2", EntityTypes.POLICY_TYPE), new Dependency("3", Policy.class, "policy3", EntityTypes.POLICY_TYPE)),
                        new Dependency("2", Policy.class, "policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", Policy.class, "policy4", EntityTypes.POLICY_TYPE))));
        bundle.addEntity(createPolicy("policy1", "1", "", "2", null, ""));
        bundle.addEntity(createPolicy("policy2", "2", "", "5", null, ""));
        bundle.addEntity(createPolicy("policy3", "3", "", null, null, ""));
        bundle.addEntity(createPolicy("policy4", "4", "", "1", null, ""));
        bundle.addEntity(createPolicy("policy5", "5", "", "4", null, ""));

        List<Policy> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(3, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "policy1".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "policy4".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "policy5".equals(c.getName())));
    }
}