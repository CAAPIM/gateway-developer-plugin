package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;

@Singleton
public class UnsupportedEntityBuilder implements EntityBuilder {
    private static final Integer ORDER = Integer.MAX_VALUE;

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        List<Entity> unsupportedEntities = new ArrayList<>();
        Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap = bundle.getUnsupportedEntities();
        if (bundleType == ENVIRONMENT) {
            unsupportedGatewayEntityMap.entrySet().forEach(entry -> {
                UnsupportedGatewayEntity unsupportedGatewayEntity = entry.getValue();
                Element resourceElement = (Element) document.adoptNode(unsupportedGatewayEntity.getElement());
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithNameMapping(unsupportedGatewayEntity.getType(),
                        unsupportedGatewayEntity.getName(), unsupportedGatewayEntity.getId(), resourceElement));
            });
        } else {
            unsupportedGatewayEntityMap.entrySet().forEach(entry -> {
                UnsupportedGatewayEntity unsupportedGatewayEntity = entry.getValue();
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithOnlyMapping(unsupportedGatewayEntity.getType(), unsupportedGatewayEntity.getName(),
                        unsupportedGatewayEntity.getId()));
            });
        }
        return unsupportedEntities;
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
