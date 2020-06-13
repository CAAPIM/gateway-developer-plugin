package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class UnsupportedEntityFilter implements EntityFilter<UnsupportedGatewayEntity> {
    private static final Set<String> IGNORE_ENTITY_TYPES = new HashSet<>();

    static {
        IGNORE_ENTITY_TYPES.add("RBAC_ROLE");
        IGNORE_ENTITY_TYPES.add("INTERFACE_TAG");
        IGNORE_ENTITY_TYPES.add("RESOURCE_ENTRY");
        IGNORE_ENTITY_TYPES.add("USER");
        IGNORE_ENTITY_TYPES.add("GROUP");
        IGNORE_ENTITY_TYPES.add("FIREWALL_RULE");
        IGNORE_ENTITY_TYPES.add("LOG_SINK");
        IGNORE_ENTITY_TYPES.add("ALERT_TRIGGER");
        IGNORE_ENTITY_TYPES.add("ALERT_ACTION");
        IGNORE_ENTITY_TYPES.add("SAMPLE_MESSAGE");
        IGNORE_ENTITY_TYPES.add("MAP_ATTRIBUTE");
        IGNORE_ENTITY_TYPES.add("MAP_IDENTITY");
        IGNORE_ENTITY_TYPES.add("MAP_TOKEN");
        IGNORE_ENTITY_TYPES.add("METRICS_BIN");
        IGNORE_ENTITY_TYPES.add("AUDIT_CONFIG");
        IGNORE_ENTITY_TYPES.add("AUDIT_MESSAGE");
        IGNORE_ENTITY_TYPES.add("AUDIT_ADMIN");
        IGNORE_ENTITY_TYPES.add("AUDIT_SYSTEM");
        IGNORE_ENTITY_TYPES.add("AUDIT_RECORD");
        IGNORE_ENTITY_TYPES.add("ESM_LOG");
        IGNORE_ENTITY_TYPES.add("ESM_NOTIFICATION_RULE");
        IGNORE_ENTITY_TYPES.add("ESM_MIGRATION_RECORD");
        IGNORE_ENTITY_TYPES.add("ESM_STANDARD_REPORT");
        IGNORE_ENTITY_TYPES.add("LICENSE_DOCUMENT");
        IGNORE_ENTITY_TYPES.add("SOLUTION_KIT");
    }

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UnsupportedGatewayEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        return bundle.getEntities(UnsupportedGatewayEntity.class).values().stream()
                .filter(p -> !IGNORE_ENTITY_TYPES.contains(p.getType())).collect(Collectors.toList());
    }
}
