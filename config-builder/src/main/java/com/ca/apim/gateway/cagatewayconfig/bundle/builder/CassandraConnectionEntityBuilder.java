/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.CASSANDRA_CONNECTION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Singleton
public class CassandraConnectionEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1300;

    private final IdGenerator idGenerator;

    @Inject
    CassandraConnectionEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return bundle.getCassandraConnections().entrySet().stream()
                        .map(e -> EntityBuilderHelper.getEntityWithOnlyMapping(CASSANDRA_CONNECTION_TYPE, e.getKey(), idGenerator.generate()))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return bundle.getCassandraConnections().entrySet().stream().map(e ->
                        buildEntity(bundle, e.getKey(), e.getValue(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    @VisibleForTesting
    Entity buildEntity(Bundle bundle, String name, CassandraConnection connection, Document document) {
        String id = idGenerator.generate();
        Element cassandraElement = createElementWithAttributesAndChildren(
                document,
                CASSANDRA_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, KEYSPACE, connection.getKeyspace()),
                createElementWithTextContent(document, CONTACT_POINT, connection.getContactPoint()),
                createElementWithTextContent(document, PORT, connection.getPort()),
                createElementWithTextContent(document, USERNAME, connection.getUsername())
        );
        if (connection.getStoredPasswordName() != null) {
            StoredPassword password = bundle.getStoredPasswords().get(connection.getStoredPasswordName());
            if (password == null) {
                throw new EntityBuilderException("Cassandra Connection is referencing missing password '" + connection.getStoredPasswordName() +"'");
            }
            cassandraElement.appendChild(createElementWithTextContent(document, PASSWORD_ID, password.getId()));
        }

        cassandraElement.appendChild(createElementWithTextContent(document, COMPRESSION, connection.getCompression()));
        cassandraElement.appendChild(createElementWithTextContent(document, SSL, connection.getSsl()));

        if (isNotEmpty(connection.getTlsCiphers())) {
            String ciphers = String.join(",", connection.getTlsCiphers());
            cassandraElement.appendChild(createElementWithTextContent(document, TLS_CIPHERS, ciphers));
        }
        cassandraElement.appendChild(createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()));

        buildAndAppendPropertiesElement(connection.getProperties(), document, cassandraElement);
        return EntityBuilderHelper.getEntityWithNameMapping(CASSANDRA_CONNECTION_TYPE, name, id, cassandraElement);
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
