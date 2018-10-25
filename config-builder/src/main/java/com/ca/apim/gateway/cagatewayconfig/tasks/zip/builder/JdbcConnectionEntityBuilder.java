/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
@Singleton
public class JdbcConnectionEntityBuilder implements EntityBuilder {

    private static final String STORED_PASSWORD_REF_FORMAT = "${secpass.%s.plaintext}";
    private static final Integer ORDER = 1000;

    private final IdGenerator idGenerator;

    @Inject
    JdbcConnectionEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return bundle.getJdbcConnections().entrySet().stream()
                        .map(e -> getEntityWithOnlyMapping(EntityTypes.JDBC_CONNECTION, e.getKey(), idGenerator.generate()))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return bundle.getJdbcConnections().entrySet().stream().map(e ->
                        buildEntity(e.getKey(), e.getValue(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    @VisibleForTesting
    Entity buildEntity(String name, JdbcConnection jdbc, Document document) {
        String id = idGenerator.generate();
        Element jdbcElement = createElementWithAttributesAndChildren(
                document,
                JDBC_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString())
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyConstants.PROPERTY_MIN_POOL_SIZE, jdbc.getMinimumPoolSize());
        properties.put(PropertyConstants.PROPERTY_MAX_POOL_SIZE, jdbc.getMaximumPoolSize());
        buildAndAppendPropertiesElement(properties, document, jdbcElement);

        Map<String, Object> connectionProperties = Optional.ofNullable(jdbc.getProperties()).orElseGet(HashMap::new);
        connectionProperties.put(PropertyConstants.PROPERTY_USER, jdbc.getUser());
        connectionProperties.put(PropertyConstants.PROPERTY_PASSWORD, String.format(STORED_PASSWORD_REF_FORMAT, jdbc.getPasswordRef()));

        jdbcElement.appendChild(createElementWithChildren(
                document,
                EXTENSION,
                createElementWithTextContent(document, DRIVER_CLASS, jdbc.getDriverClass()),
                createElementWithTextContent(document, JDBC_URL, jdbc.getJdbcUrl()),
                buildPropertiesElement(connectionProperties, document, CONNECTION_PROPERTIES)
        ));

        return getEntityWithNameMapping(EntityTypes.JDBC_CONNECTION, name, id, jdbcElement);
    }

    @Override
    @NotNull
    public Integer getOrder() {
        return ORDER;
    }

}
