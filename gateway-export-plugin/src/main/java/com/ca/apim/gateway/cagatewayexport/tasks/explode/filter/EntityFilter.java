package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface EntityFilter<E extends GatewayEntity> {

    /**
     * @return the position of the entity filter, has to be non null and not conflicting
     */
    @NotNull
    Collection<Class<? extends EntityFilter>> getDependencyEntityFilters();

    /**
     * Loads an instance of an entity filter of the given class type.
     *
     * @param filterClass The entity filter class
     * @return an instance of the entity filter
     */
    static EntityFilter getEntityFilterFromClass(Class<? extends EntityFilter> filterClass) {
        return InjectionRegistry.getInstance(filterClass);
    }

    /**
     * Filters entities in the bundle and returns the list of filtered entities. Filtered entities are entities that are meant to be kept.
     *
     * @param folderPath          The folder path that will be filtered
     * @param filterConfiguration This is the filter configuration.
     * @param bundle              The bundle to filter entities from
     * @param filteredBundle      The filtered bundle containing already filtered entities.
     * @return The list of entities filtered from the bundle
     */
    List<E> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle);

    /**
     * The filterable entity name. This is used to validate the filter configuration only container filterable configurations
     *
     * @return The name of the filterable entity. The empty string if it isn't filterable from a filter configuration.
     */
    default String getFilterableEntityName() {
        return "";
    }
}
