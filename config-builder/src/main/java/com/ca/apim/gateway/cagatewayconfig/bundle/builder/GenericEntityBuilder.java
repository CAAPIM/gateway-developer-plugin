package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class GenericEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1700;
    private final IdGenerator idGenerator;
    private final DocumentTools documentTools;

    @Inject
    public GenericEntityBuilder(IdGenerator idGenerator, DocumentTools documentTools) {
        this.idGenerator = idGenerator;
        this.documentTools = documentTools;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        Map<String, GenericEntity> genericEntities =
                Optional.ofNullable(bundle.getGenericEntities()).orElse(Collections.emptyMap());
        return buildEntities(genericEntities, bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, GenericEntity> genericEntities, Bundle bundle,
                                       BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return genericEntities.entrySet().stream()
                        .map(entry -> getEntityWithOnlyMapping(EntityTypes.GENERIC_TYPE, entry.getKey(),
                                generateId(entry.getValue())))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return genericEntities.entrySet().stream()
                        .map(e -> buildGenericEntity(e.getKey(), e.getValue(), document))
                        .collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    private Entity buildGenericEntity(String name, GenericEntity genericEntity, Document document) {
        String id = generateId(genericEntity);
        Element genericEntityElement = createElementWithAttributesAndChildren(
                document,
                GENERIC_ENTITY,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, DESCRIPTION, genericEntity.getDescription()),
                createElementWithTextContent(document, ENTITY_CLASS_NAME, genericEntity.getEntityClassName()),
                // Enabled element must be after Description and EntityClassName
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()),
                createElementWithTextContent(document, VALUE_XML, genericEntity.getValueXml())
        );

        return EntityBuilderHelper.getEntityWithNameMapping(EntityTypes.GENERIC_TYPE, name, id, genericEntityElement);
    }

    private String generateId(GenericEntity genericEntity) {
        if (genericEntity != null && genericEntity.getAnnotatedEntity() != null && StringUtils.isNotBlank(genericEntity.getAnnotatedEntity().getId())) {
            return genericEntity.getAnnotatedEntity().getId();
        }
        return idGenerator.generate();
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
