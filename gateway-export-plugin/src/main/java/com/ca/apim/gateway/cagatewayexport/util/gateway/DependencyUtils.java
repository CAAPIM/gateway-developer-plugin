package com.ca.apim.gateway.cagatewayexport.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterException;
import org.jetbrains.annotations.NotNull;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils;
import java.util.*;
import java.util.function.Predicate;
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
     * @param includeEntity       A predicate to force inclusion of an entity
     * @param <E>                 The entity type
     * @return The list of filtered entities
     */
    @NotNull
    public static <E extends GatewayEntity> List<E> filterDependencies(Class<E> dependentEntityType, Bundle bundle, Bundle filteredBundle, Predicate<E> includeEntity) {
        // Gets entities of the given type that are dependencies of entities in the filteredBundle
        Set<Dependency> dependentEntities = DependencyUtils.filterDependencies(dependentEntityType, bundle.getDependencyMap(), filteredBundle);

        return bundle.getEntities(dependentEntityType).values().stream()
                //keep only entities the are dependencies of entities in the filtered bundle
                .filter(entity -> dependentEntities.contains(new Dependency(entity.getId(), dependentEntityType, entity.getName(), EntityUtils.getEntityType(dependentEntityType))) || includeEntity.test(entity))
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
    private static <E extends GatewayEntity> Set<Dependency> filterDependencies(Class<E> dependentEntityType, Map<Dependency, List<Dependency>> dependencies, Bundle bundle) {
        return dependencies.entrySet().stream()
                // filter out dependencies that are not in the bundle
                .filter(e -> bundle.getEntities(e.getKey().getTypeClass()).get(e.getKey().getId()) != null)
                // keep only the dependencies
                .flatMap(e -> e.getValue().stream())
                // keep only dependencies that are of the given type.
                .filter(d -> d.getTypeClass() == dependentEntityType)
                .collect(Collectors.toSet());
    }

    /**
     * Validates that all entities with names in the entityNames sets are in the entities list. If there are entities missing a `EntityFilterException` is thrown
     *
     * @param entities    The list of entities to search through
     * @param entityNames The entity names to ensure are in the entities list
     * @param entityName  The name of the entity. Used in an exception message
     * @param <E>         The entity Type
     */
    public static <E extends GatewayEntity> void validateEntitiesInList(List<E> entities, Collection<String> entityNames, final String entityName) {
        Set<String> missingEntities = new HashSet<>();
        entityNames.forEach(name -> {
            if (entities.stream().noneMatch(c -> name.equals(c.getName()))) {
                missingEntities.add(name);
            }
        });
        if (!missingEntities.isEmpty()) {
            throw new EntityFilterException("Missing " + entityName + " with name: '" + String.join("', '", missingEntities) + "'");
        }
    }
}
