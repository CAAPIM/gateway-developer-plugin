/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_MAX_POOL_SIZE;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_MIN_POOL_SIZE;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class JdbcConnectionLoader implements EntityLoader<JdbcConnectionEntity> {

    @Override
    public JdbcConnectionEntity load(Element element) {
        final Element jdbcConnection = getSingleChildElement(getSingleChildElement(element, RESOURCE), JDBC_CONNECTION);

        final String name = getSingleChildElementTextContent(jdbcConnection, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(jdbcConnection, PROPERTIES, true), PROPERTIES);
        final Integer minPoolSize = (Integer) properties.get(PROPERTY_MIN_POOL_SIZE);
        final Integer maxPoolSize = (Integer) properties.get(PROPERTY_MAX_POOL_SIZE);

        final Element extension = getSingleChildElement(jdbcConnection, EXTENSION);
        final String driverClass = getSingleChildElementTextContent(extension, DRIVER_CLASS);
        final String jdbcUrl = getSingleChildElementTextContent(extension, JDBC_URL);
        final Map<String, Object> connectionProperties = mapPropertiesElements(getSingleChildElement(extension, CONNECTION_PROPERTIES, true), CONNECTION_PROPERTIES);
        final String user = (String) connectionProperties.remove(PropertyConstants.PROPERTY_USER);
        final String passwordRef = (String) connectionProperties.remove(PropertyConstants.PROPERTY_PASSWORD);
        if (StringUtils.isEmpty(passwordRef)) {
            throw new WriteException("JDBC Connection " + name + " is missing reference for a stored password.");
        }

        return new JdbcConnectionEntity.Builder()
                .id(jdbcConnection.getAttribute(ATTRIBUTE_ID))
                .name(name)
                .user(user)
                .minimumPoolSize(minPoolSize)
                .maximumPoolSize(maxPoolSize)
                .passwordRef(passwordRef)
                .properties(connectionProperties)
                .driverClass(driverClass)
                .jdbcUrl(jdbcUrl)
                .build();
    }

    @Override
    public Class<JdbcConnectionEntity> entityClass() {
        return JdbcConnectionEntity.class;
    }
}
