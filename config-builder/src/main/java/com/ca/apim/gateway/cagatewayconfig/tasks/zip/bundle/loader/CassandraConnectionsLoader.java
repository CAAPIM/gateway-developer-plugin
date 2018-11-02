/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Singleton
public class CassandraConnectionsLoader implements BundleDependencyLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element cassandraConnectionElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), CASSANDRA_CONNECTION);

        final String name = getSingleChildElementTextContent(cassandraConnectionElement, NAME);
        CassandraConnection cassandraConnection = new CassandraConnection();
        cassandraConnection.setName(name);
        cassandraConnection.setKeyspace(getSingleChildElementTextContent(cassandraConnectionElement, KEYSPACE));
        cassandraConnection.setCompression(getSingleChildElementTextContent(cassandraConnectionElement, COMPRESSION));
        cassandraConnection.setContactPoint(getSingleChildElementTextContent(cassandraConnectionElement, CONTACT_POINT));
        cassandraConnection.setPort(Integer.parseInt(getSingleChildElementTextContent(cassandraConnectionElement, PORT)));
        cassandraConnection.setProperties(mapPropertiesElements(getSingleChildElement(cassandraConnectionElement, PROPERTIES, true), PROPERTIES));
        cassandraConnection.setSsl(Boolean.parseBoolean(getSingleChildElementTextContent(cassandraConnectionElement, SSL)));
        cassandraConnection.setUsername(getSingleChildElementTextContent(cassandraConnectionElement, USERNAME));

        final Element passwordIdElement = getSingleChildElement(cassandraConnectionElement, PASSWORD_ID, true);
        cassandraConnection.setPasswordId(passwordIdElement != null ? passwordIdElement.getTextContent() : null);

        Set<String> ciphers = null;
        final Element tlsCiphersElement = getSingleChildElement(cassandraConnectionElement, TLS_CIPHERS, true);
        final String ciphersAsString = tlsCiphersElement != null ? tlsCiphersElement.getTextContent() : null;
        if (isNotEmpty(ciphersAsString)) {
            ciphers = new HashSet<>(Arrays.asList(ciphersAsString.split(",")));
        }
        cassandraConnection.setTlsCiphers(ciphers);
        cassandraConnection.setProperties(mapPropertiesElements(getSingleChildElement(cassandraConnectionElement, PROPERTIES, true), PROPERTIES));

        bundle.getCassandraConnections().put(name, cassandraConnection);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.CASSANDRA_CONNECTION_TYPE;
    }
}
