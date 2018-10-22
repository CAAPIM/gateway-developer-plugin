package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EntityFiltersTest {

    @Test
    void testDependencies() {
        assertFalse(EntityFilter.isDependency(new TestEntityFilter1(), Sets.newHashSet()));


        Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
                TestEntityFilter2.class).collect(Collectors.toSet());

        assertTrue(EntityFilter.isDependency(new TestEntityFilter1(), FILTER_DEPENDENCIES));

    }

    @Test
    void testRegistry() {

        EntityFilterRegistry entityFilterRegistry = new EntityFilterRegistry(Stream.of(
                new TestEntityFilter1(), new TestEntityFilter2(), new TestEntityFilter3()).collect(Collectors.toSet()));
        Iterator<EntityFilter> filters = entityFilterRegistry.getEntityFilters().iterator();
        assertEquals(TestEntityFilter1.class, filters.next().getClass());
        assertEquals(TestEntityFilter3.class, filters.next().getClass());
        assertEquals(TestEntityFilter2.class, filters.next().getClass());

        entityFilterRegistry = new EntityFilterRegistry(Stream.of(
                new TestEntityFilter3(), new TestEntityFilter2(), new TestEntityFilter1()).collect(Collectors.toSet()));
        filters = entityFilterRegistry.getEntityFilters().iterator();
        assertEquals(TestEntityFilter1.class, filters.next().getClass());
        assertEquals(TestEntityFilter3.class, filters.next().getClass());
        assertEquals(TestEntityFilter2.class, filters.next().getClass());

        entityFilterRegistry = new EntityFilterRegistry(Stream.of(
                new TestEntityFilter2(), new TestEntityFilter1(), new TestEntityFilter3()).collect(Collectors.toSet()));
        filters = entityFilterRegistry.getEntityFilters().iterator();
        assertEquals(TestEntityFilter1.class, filters.next().getClass());
        assertEquals(TestEntityFilter3.class, filters.next().getClass());
        assertEquals(TestEntityFilter2.class, filters.next().getClass());
    }

    static abstract class TestEntityFilterBase<E extends Entity> {

        private final Collection<Class<? extends EntityFilter>> dependencies;

        public TestEntityFilterBase() {
            dependencies = Collections.emptySet();
        }

        public TestEntityFilterBase(Collection<Class<? extends EntityFilter>> dependencies) {
            this.dependencies = dependencies;
        }

        public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
            return dependencies;
        }

        public List<E> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
            return Collections.emptyList();
        }
    }


    static class TestEntityFilter1 extends TestEntityFilterBase<Entity> implements EntityFilter<Entity> {
    }

    static class TestEntityFilter2 extends TestEntityFilterBase<Entity> implements EntityFilter<Entity> {
        public TestEntityFilter2() {
            super(Stream.of(
                    TestEntityFilter3.class).collect(Collectors.toSet()));
        }
    }

    static class TestEntityFilter3 extends TestEntityFilterBase<Entity> implements EntityFilter<Entity> {
        public TestEntityFilter3() {
            super(Stream.of(
                    TestEntityFilter1.class).collect(Collectors.toSet()));
        }
    }
}