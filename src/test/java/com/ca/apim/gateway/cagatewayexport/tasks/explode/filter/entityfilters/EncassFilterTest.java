package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Encass;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EncassFilterTest {
    @Test
    void filterNoEntities() {
        EncassFilter filter = new EncassFilter();

        Bundle filteredBundle = new Bundle();
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(Collections.emptyMap());

        List<EncassEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(0, filteredEntities.size());
    }

    @Test
    void filter() {
        EncassFilter filter = new EncassFilter();

        Bundle filteredBundle = new Bundle();
        filteredBundle.addEntity(new PolicyEntity("my-policy", "1", "", "", null, ""));
        Bundle bundle = FilterTestUtils.getBundle();
        bundle.setDependencies(
                ImmutableMap.of(
                        new Dependency("1", PolicyEntity.class), Arrays.asList(new Dependency("2", EncassEntity.class), new Dependency("3", EncassEntity.class)),
                        new Dependency("2", PolicyEntity.class), Collections.singletonList(new Dependency("4", EncassEntity.class))));
        bundle.addEntity(new EncassEntity("encass1", "1", "", "zzz", null, null ));
        bundle.addEntity(new EncassEntity("encass2", "2", "", "1", null, null ));
        bundle.addEntity(new EncassEntity("encass3", "3", "", "1", null, null ));
        bundle.addEntity(new EncassEntity("encass4", "4", "", "2", null, null ));

        List<EncassEntity> filteredEntities = filter.filter("/my/folder/path", bundle, filteredBundle);

        assertEquals(2, filteredEntities.size());
        assertTrue(filteredEntities.stream().anyMatch(c -> "encass2".equals(c.getName())));
        assertTrue(filteredEntities.stream().anyMatch(c -> "encass3".equals(c.getName())));
    }
}