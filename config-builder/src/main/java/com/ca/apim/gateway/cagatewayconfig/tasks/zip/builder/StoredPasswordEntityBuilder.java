/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

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
    public List<Entity> build(Bundle bundle, Document document) {
        return bundle.getStoredPasswords().entrySet().stream().map(e -> buildStoredPasswordEntity(e.getKey(), e.getValue(), document)).collect(toList());
    }

    private Entity buildStoredPasswordEntity(String name, StoredPassword storedPassword, Document document) {
        String id = idGenerator.generate();

        Element storedPasswordElement = createElementWithAttribute(document, STORED_PASSWD, ATTRIBUTE_ID, id);
        storedPasswordElement.appendChild(createElementWithTextContent(document, NAME, name));
        storedPasswordElement.appendChild(createElementWithTextContent(document, PASSWORD, storedPassword.getPassword()));

        buildAndAppendPropertiesElement(storedPassword.getProperties(), document, storedPasswordElement);

        return new Entity(STORED_PASSWORD_TYPE, name, id, storedPasswordElement);
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }
}
