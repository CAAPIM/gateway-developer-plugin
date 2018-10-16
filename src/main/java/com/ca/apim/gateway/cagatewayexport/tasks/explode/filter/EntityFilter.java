package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface EntityFilter<E extends Entity> extends Comparable<EntityFilter> {

    /**
     * @return the position of the entity filter, has to be non null and not conflicting
     */
    @NotNull
    Collection<Class<? extends EntityFilter>> getDependencyEntityFilters();

    /**
     * Compares this entity filter to another entity filter. Returns 0 if they are the same instance. -1 if this filter is a dependency of the given one. 1 otherwise
     *
     * @param e The entity filter to compare to
     * @return 0 if they are the same instance. -1 if this filter is a dependency of the given one. 1 otherwise
     */
    @Override
    default int compareTo(@NotNull EntityFilter e) {
        if (this == e) {
            return 0;
        }
        return isDependency(this, e.getDependencyEntityFilters()) ? -1 : 1;
    }

    /**
     * Checks is the given entity filter depends on an entity filter in the given collection.
     *
     * @param entityFilter  The entity filter to check if it is a dependency
     * @param entityFilters The entity filters to scan for dependencies
     * @return true if the given entity filter is a dependency of the entity filters in the given collection. false otherwise
     */
    static boolean isDependency(EntityFilter entityFilter, Collection<Class<EntityFilter<? extends Entity>>> entityFilters) {
        return entityFilters.stream().anyMatch(e -> e.equals(entityFilter.getClass()) || isDependency(entityFilter, getEntityFilterFromClass(e).getDependencyEntityFilters()));
    }

    /**
     * Loads an instance of an entity filter of the given class type.
     *
     * @param filterClass The entity filter class
     * @return an instance of the entity filter
     */
    static EntityFilter getEntityFilterFromClass(Class<EntityFilter<? extends Entity>> filterClass) {
        return ExportPluginModule.getInjector().getInstance(filterClass);
    }

    /**
     * Filters entities in the bundle and returns the list of filtered entities. Filtered entities are entities that are meant to be kept.
     *
     * @param folderPath     The folder path that will be filtered
     * @param bundle         The bundle to filter entities from
     * @param filteredBundle The filtered bundle containing already filtered entities.
     * @return The list of entities filtered from the bundle
     */
    List<E> filter(String folderPath, Bundle bundle, Bundle filteredBundle);
}
