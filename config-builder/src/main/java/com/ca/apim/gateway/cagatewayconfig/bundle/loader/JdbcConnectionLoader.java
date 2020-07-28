/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class JdbcConnectionLoader implements BundleEntityLoader {

    @VisibleForTesting
    public JdbcConnectionLoader() {
        //
    }

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element jdbcConnectionElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), JDBC_CONNECTION);

        final String name = getSingleChildElementTextContent(jdbcConnectionElement, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(jdbcConnectionElement, PROPERTIES, true), PROPERTIES);
        final Integer minPoolSize = (Integer) properties.remove(PROPERTY_MIN_POOL_SIZE);
        final Integer maxPoolSize = (Integer) properties.remove(PROPERTY_MAX_POOL_SIZE);

        final Element extension = getSingleChildElement(jdbcConnectionElement, EXTENSION);
        final String driverClass = getSingleChildElementTextContent(extension, DRIVER_CLASS);
        final String jdbcUrl = getSingleChildElementTextContent(extension, JDBC_URL);
        final Map<String, Object> connectionProperties = mapPropertiesElements(getSingleChildElement(extension, CONNECTION_PROPERTIES, true), CONNECTION_PROPERTIES);
        final String user = (String) connectionProperties.remove(PROPERTY_USER);
        final String password = (String) connectionProperties.remove(PROPERTY_PASSWORD);

        JdbcConnection jdbcConnection = new JdbcConnection();
        jdbcConnection.setName(name);
        jdbcConnection.setId(jdbcConnectionElement.getAttribute(ATTRIBUTE_ID));
        jdbcConnection.setJdbcUrl(jdbcUrl);
        jdbcConnection.setDriverClass(driverClass);
        jdbcConnection.setUser(user);
        jdbcConnection.setPassword(password);
        jdbcConnection.setMinimumPoolSize(minPoolSize);
        jdbcConnection.setMaximumPoolSize(maxPoolSize);
        jdbcConnection.setProperties(connectionProperties);

        Set<Annotation> annotations = new HashSet<>();
        Annotation bundleEntity = new Annotation(AnnotationType.BUNDLE_HINTS);
        bundleEntity.setId(jdbcConnectionElement.getAttribute(ATTRIBUTE_ID));
        annotations.add(bundleEntity);
        jdbcConnection.setAnnotations(annotations);

        bundle.getJdbcConnections().put(name, jdbcConnection);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.JDBC_CONNECTION;
    }
}
