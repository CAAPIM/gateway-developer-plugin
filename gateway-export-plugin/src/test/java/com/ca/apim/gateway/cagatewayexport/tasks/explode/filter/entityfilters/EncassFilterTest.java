package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createEncass;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncassFilterTest {
    @Test
    void filterNoEntities() {
        EncassFilter filter = new EncassFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(Collections.emptyMap());

        List<Encass> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        EncassFilter filter = new EncassFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(createPolicy("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencyMap(
                ImmutableMap.of(
                        new Dependency("1", Policy.class, "my-policy", EntityTypes.POLICY_TYPE), Arrays.asList(new Dependency("2", Encass.class, "encass2", EntityTypes.ENCAPSULATED_ASSERTION_TYPE), new Dependency("3", Encass.class, "encass1", EntityTypes.ENCAPSULATED_ASSERTION_TYPE)),
                        new Dependency("2", Policy.class, "my-policy2", EntityTypes.POLICY_TYPE), Collections.singletonList(new Dependency("4", Encass.class, "encass4", EntityTypes.ENCAPSULATED_ASSERTION_TYPE))));
        bundle.addEntity(createEncass("encass1", "1", "", "zzz"));
        bundle.addEntity(createEncass("encass2", "2", "", "1"));
        bundle.addEntity(createEncass("encass3", "3", "", "1"));
        bundle.addEntity(createEncass("encass4", "4", "", "2"));

        List<Encass> filteredEntities = filter.filter("/my/folder/path", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "encass2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "encass3".equals(c.getName())));
    }
}