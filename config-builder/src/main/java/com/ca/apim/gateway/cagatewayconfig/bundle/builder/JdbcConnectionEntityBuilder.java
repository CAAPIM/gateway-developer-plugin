/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.STORED_PASSWORD_REF_FORMAT;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
@Singleton
public class JdbcConnectionEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1000;

    private final IdGenerator idGenerator;

    @Inject
    JdbcConnectionEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        Map<String, JdbcConnection> jdbcConnectionMap = Optional.ofNullable(bundle.getJdbcConnections()).orElse(Collections.emptyMap());
       return buildEntities(jdbcConnectionMap, bundle, bundleType, document);
    }
    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return entities.entrySet().stream()
                        .map(e -> EntityBuilderHelper.getEntityWithOnlyMapping(EntityTypes.JDBC_CONNECTION, bundle.applyUniqueName(e.getKey(), BundleType.ENVIRONMENT, false), idGenerator.generate()))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return entities.entrySet().stream().map(e ->
                        buildEntity(bundle.applyUniqueName(e.getKey(), bundleType, false), (JdbcConnection)e.getValue(), document, bundle)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    @VisibleForTesting
    Entity buildEntity(String name, JdbcConnection jdbc, Document document, Bundle bundle) {
        String id = idGenerator.generate();
        Element jdbcElement = createElementWithAttributesAndChildren(
                document,
                JDBC_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString())
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_MIN_POOL_SIZE, jdbc.getMinimumPoolSize());
        properties.put(PROPERTY_MAX_POOL_SIZE, jdbc.getMaximumPoolSize());
        buildAndAppendPropertiesElement(properties, document, jdbcElement);

        Map<String, Object> connectionProperties = Optional.ofNullable(jdbc.getProperties()).orElseGet(HashMap::new);
        connectionProperties.put(PROPERTY_USER, jdbc.getUser());
        if (jdbc.getPasswordRef() != null) {
            connectionProperties.put(PROPERTY_PASSWORD, String.format(STORED_PASSWORD_REF_FORMAT, bundle.applyUniqueName(jdbc.getPasswordRef(), BundleType.ENVIRONMENT, true)));
        } else {
            connectionProperties.put(PROPERTY_PASSWORD, jdbc.getPassword());
        }

        jdbcElement.appendChild(createElementWithChildren(
                document,
                EXTENSION,
                createElementWithTextContent(document, DRIVER_CLASS, jdbc.getDriverClass()),
                createElementWithTextContent(document, JDBC_URL, jdbc.getJdbcUrl()),
                buildPropertiesElement(connectionProperties, document, CONNECTION_PROPERTIES)
        ));

        return EntityBuilderHelper.getEntityWithNameMapping(EntityTypes.JDBC_CONNECTION, name, id, jdbcElement);
    }

    @Override
    @NotNull
    public Integer getOrder() {
        return ORDER;
    }

}
