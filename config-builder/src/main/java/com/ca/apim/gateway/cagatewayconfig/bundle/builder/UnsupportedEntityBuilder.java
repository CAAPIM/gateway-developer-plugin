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
        unsupportedGatewayEntityMap.forEach((key, value) -> {
            if (bundleType != ENVIRONMENT || value.isExcluded()) {
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithOnlyMapping(value.getType(), value.getName()
                        , value.getId()));
            } else {
                Element resourceElement = (Element) document.adoptNode(value.getElement());
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithNameMapping(value.getType(), value.getName()
                        , value.getId(), resourceElement));
            }
        });
        return unsupportedEntities;
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
