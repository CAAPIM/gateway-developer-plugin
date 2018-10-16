package com.ca.apim.gateway.cagatewayexport.util.gateway;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyUtils {

    private DependencyUtils() {
    }

    /**
     * Filters dependencies of the given type from the given bundle. This filter will keep only entities of the given type that are dependencies of other entities in the filtered bundle.
     *
     * @param dependentEntityType The dependent entity type to filter out
     * @param bundle              The bundle to filter entities from
     * @param filteredBundle      The filtered bundle to look through to find dependencies in
     * @param <E>                 The entity type
     * @return The list of filtered entities
     */
    @NotNull
    public static <E extends Entity> List<E> filterDependencies(Class<E> dependentEntityType, Bundle bundle, Bundle filteredBundle) {
        // Gets entities of the given type that are dependencies of entities in the filteredBundle
        Set<Dependency> dependentClusterProperties = DependencyUtils.filterDependencies(dependentEntityType, bundle.getDependencies(), filteredBundle);

        return bundle.getEntities(dependentEntityType).values().stream()
                //keep only entities the are dependencies of entities in the filtered bundle
                .filter(c -> dependentClusterProperties.contains(new Dependency(c.getId(), dependentEntityType)))
                .collect(Collectors.toList());
    }

    /**
     * Finds all entities of the given type from the dependency map that are dependencies of entities in the given bundle.
     *
     * @param dependentEntityType The entity type to find in the dependency map
     * @param dependencies        The dependency map to search
     * @param bundle              Will return all entities of `dependentEntityType` that are dependencies of entities in this bundle
     * @param <E>                 The entity type
     * @return A set of dependencies of type `dependentEntityType` that are dependencies of entities in the bundle.
     */
    private static <E extends Entity> Set<Dependency> filterDependencies(Class<E> dependentEntityType, Map<Dependency, List<Dependency>> dependencies, Bundle bundle) {
        return dependencies.entrySet().stream()
                // filter out dependencies that are not in the bundle
                .filter(e -> bundle.getEntities(e.getKey().getType()).get(e.getKey().getId()) != null)
                // keep only the dependencies
                .flatMap(e -> e.getValue().stream())
                // keep only dependencies that are of the given type.
                .filter(d -> d.getType() == dependentEntityType)
                .collect(Collectors.toSet());
    }
}
