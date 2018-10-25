/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Singleton
public class CassandraConnectionLoader implements EntityLoader<CassandraConnectionEntity> {

    @Override
    public CassandraConnectionEntity load(Element element) {
        final Element cassandraConnection = getSingleChildElement(getSingleChildElement(element, RESOURCE), CASSANDRA_CONNECTION);

        final String name = getSingleChildElementTextContent(cassandraConnection, NAME);
        final String keyspace = getSingleChildElementTextContent(cassandraConnection, KEYSPACE);
        final String contactPoint = getSingleChildElementTextContent(cassandraConnection, CONTACT_POINT);
        final Integer port = Integer.parseInt(getSingleChildElementTextContent(cassandraConnection, PORT));
        final String username = getSingleChildElementTextContent(cassandraConnection, USERNAME);
        final Element passwordIdElement = getSingleChildElement(cassandraConnection, PASSWORD_ID, true);
        final String passwordId = passwordIdElement != null ? passwordIdElement.getTextContent() : null;
        final String compression = getSingleChildElementTextContent(cassandraConnection, COMPRESSION);
        final Boolean ssl = Boolean.parseBoolean(getSingleChildElementTextContent(cassandraConnection, SSL));
        Set<String> ciphers = null;
        final Element tlsCiphersElement = getSingleChildElement(cassandraConnection, TLS_CIPHERS, true);
        final String ciphersAsString = tlsCiphersElement != null ? tlsCiphersElement.getTextContent() : null;
        if (isNotEmpty(ciphersAsString)) {
            ciphers = new HashSet<>(Arrays.asList(ciphersAsString.split(",")));
        }
        Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(cassandraConnection, PROPERTIES, true), PROPERTIES);
        if (properties.isEmpty()) {
            properties = null;
        }

        return new CassandraConnectionEntity.Builder()
                .id(cassandraConnection.getAttribute(ATTRIBUTE_ID))
                .name(name)
                .username(username)
                .keyspace(keyspace)
                .contactPoint(contactPoint)
                .port(port)
                .passwordId(passwordId)
                .compression(compression)
                .ssl(ssl)
                .tlsCiphers(ciphers)
                .properties(properties)
                .build();
    }

    @Override
    public Class<CassandraConnectionEntity> entityClass() {
        return CassandraConnectionEntity.class;
    }
}
