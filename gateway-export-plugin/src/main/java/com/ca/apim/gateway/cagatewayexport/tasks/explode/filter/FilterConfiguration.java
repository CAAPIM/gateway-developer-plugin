package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FilterConfiguration {

    private Map<String, Collection<String>> entityFilters = new HashMap<>();

    public Collection<String> getRequiredEntityNames(String entityType) {
        return entityFilters.getOrDefault(entityType, Collections.emptySet());
    }

    public Map<String, Collection<String>> getEntityFilters() {
        return entityFilters;
    }

    public void setEntityFilters(Map<String, Collection<String>> entityFilters) {
        this.entityFilters.putAll(entityFilters);
    }
}
