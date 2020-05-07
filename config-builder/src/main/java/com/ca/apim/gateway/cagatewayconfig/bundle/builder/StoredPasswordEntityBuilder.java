/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.STORED_PASSWORD_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.util.stream.Collectors.toList;

@Singleton
public class StoredPasswordEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 900;
    private final IdGenerator idGenerator;

    @Inject
    StoredPasswordEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return buildEntities(bundle.getStoredPasswords(), bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return entities.entrySet().stream()
                        .map(e -> EntityBuilderHelper.getEntityWithOnlyMapping(STORED_PASSWORD_TYPE, e.getKey(), idGenerator.generate()))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return entities.entrySet().stream().map(e -> buildStoredPasswordEntity(e.getKey(), (StoredPassword)e.getValue(), document)).collect(toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    @Override
    public List<Entity> build(Map<Class, Map<String, GatewayEntity>> entityMap, AnnotatedEntity annotatedEntity, Bundle bundle, BundleType bundleType, Document document) {
        Map<String, GatewayEntity> map = Optional.ofNullable(entityMap.get(StoredPassword.class)).orElse(Collections.emptyMap());
        return buildEntities(map, bundle, bundleType, document);
    }

    private Entity buildStoredPasswordEntity(String name, StoredPassword storedPassword, Document document) {
        String id = idGenerator.generate();
        storedPassword.setId(id);

        Element storedPasswordElement = createElementWithAttribute(document, STORED_PASSWD, ATTRIBUTE_ID, id);
        storedPasswordElement.appendChild(createElementWithTextContent(document, NAME, name));
        storedPasswordElement.appendChild(createElementWithTextContent(document, PASSWORD, storedPassword.getPassword()));

        buildAndAppendPropertiesElement(storedPassword.getProperties(), document, storedPasswordElement);

        return EntityBuilderHelper.getEntityWithNameMapping(STORED_PASSWORD_TYPE, name, id, storedPasswordElement);
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }
}
