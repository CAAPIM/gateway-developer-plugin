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
        if (bundle instanceof AnnotatedBundle) {
            AnnotatedBundle annotatedBundle = (AnnotatedBundle) bundle;
            Map<String, UnsupportedGatewayEntity> map = Optional.ofNullable(bundle.getUnsupportedEntities()).orElse(Collections.emptyMap());
            return buildEntities(map, annotatedBundle, annotatedBundle.getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getUnsupportedEntities(), null, bundle, bundleType, document);
        }
    }

    public List<Entity> buildEntities(Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap, AnnotatedBundle annotatedBundle, Bundle bundle, BundleType bundleType, Document document) {
        List<Entity> unsupportedEntities = new ArrayList<>();
        if (bundleType == ENVIRONMENT) {
            unsupportedGatewayEntityMap.entrySet().forEach(entry -> {
                UnsupportedGatewayEntity unsupportedGatewayEntity = entry.getValue();
                Element resourceElement = (Element) document.adoptNode(unsupportedGatewayEntity.getElement());
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithNameMapping(unsupportedGatewayEntity.getType(),
                        entry.getKey(), unsupportedGatewayEntity.getId(), resourceElement));
            });
        } else {
            unsupportedGatewayEntityMap.entrySet().forEach(entry -> {
                UnsupportedGatewayEntity unsupportedGatewayEntity = entry.getValue();
                unsupportedEntities.add(EntityBuilderHelper.getEntityWithOnlyMapping(unsupportedGatewayEntity.getType(), entry.getKey(),
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
