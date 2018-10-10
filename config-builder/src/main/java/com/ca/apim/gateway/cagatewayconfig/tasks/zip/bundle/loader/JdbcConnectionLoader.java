/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class JdbcConnectionLoader implements BundleDependencyLoader {

    JdbcConnectionLoader() {
    }

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element jdbcConnectionElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), JDBC_CONNECTION);
        final String name = getSingleChildElementTextContent(jdbcConnectionElement, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(jdbcConnectionElement, PROPERTIES, true), PROPERTIES);

        JdbcConnection jdbcConnection = new JdbcConnection();
        jdbcConnection.setProperties(properties);
        bundle.getJdbcConnections().put(name, jdbcConnection);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.JDBC_CONNECTION;
    }
}
