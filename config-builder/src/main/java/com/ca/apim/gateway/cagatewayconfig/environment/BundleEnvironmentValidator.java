/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

class BundleEnvironmentValidator {

    private final Bundle environmentBundle;
    private DocumentTools documentTools = DocumentTools.INSTANCE;
    private DocumentFileUtils documentFileUtils = DocumentFileUtils.INSTANCE;

    BundleEnvironmentValidator(Bundle environmentBundle) {
        this.environmentBundle = environmentBundle;
    }

    /**
     * Validate that all required environment is available in the environment bundle
     *  @param bundleName       The name of the bundle to look for environment requirements in
     * @param deploymentBundle The deployment bundle to look for enviornment requirements in
     * @param mode The generation mode, where its coming from.
     */
    void validateEnvironmentProvided(String bundleName, String deploymentBundle, EnvironmentBundleCreationMode mode) {
        Document deploymentBundleDocument;
        try {
            deploymentBundleDocument = documentTools.parse(deploymentBundle);
        } catch (DocumentParseException e) {
            throw new DeploymentBundleException("Unable to parse deployment bundle: " + bundleName);
        }

        Element mappingElement = getSingleChildElement(deploymentBundleDocument.getDocumentElement(), MAPPINGS);
        List<Element> mappingElements = getChildElements(mappingElement, MAPPING);
        mappingElements.forEach(mapping -> {
            Element propertiesElement = getSingleChildElement(mapping, PROPERTIES, true);
            if (propertiesElement != null) {
                validateElement(mode, mapping, propertiesElement);
            }
        });
    }

    private void validateElement(EnvironmentBundleCreationMode mode, Element mapping, Element propertiesElement) {
        List<Element> propertyElements = getChildElements(propertiesElement, PROPERTY);
        if (propertyElements.stream().anyMatch(p -> FAIL_ON_NEW.equals(p.getAttribute(ATTRIBUTE_KEY)) && Boolean.valueOf(getSingleChildElementTextContent(p, BOOLEAN_VALUE)))) {
            boolean mapByName = propertyElements.stream().anyMatch(p -> MAP_BY.equals(p.getAttribute(ATTRIBUTE_KEY)) && MappingProperties.NAME.equals(getSingleChildElementTextContent(p, STRING_VALUE)));
            if (!mapByName) {
                throw new DeploymentBundleException("Expected mapping to be map by name: " + documentFileUtils.elementToString(mapping));
            }
            List<Element> mapToProperties = propertyElements.stream().filter(p -> MAP_TO.equals(p.getAttribute(ATTRIBUTE_KEY))).collect(Collectors.toList());
            String mapToName = getSingleChildElementTextContent(mapToProperties.get(0), STRING_VALUE);

            String type = mapping.getAttribute(ATTRIBUTE_TYPE);
            if (mode.isRequired(type)) {
                findInBundle(environmentBundle, type, mapToName, mode);
            }
        }
    }

    private void findInBundle(Bundle bundle, String type, String name, EnvironmentBundleCreationMode mode) {
        switch (type) {
            case EntityTypes.CLUSTER_PROPERTY_TYPE:
                if (bundle.getEnvironmentProperties().get(PREFIX_GATEWAY + name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for property: " + name);
                }
                break;
            case EntityTypes.ID_PROVIDER_CONFIG_TYPE:
                if (bundle.getIdentityProviders().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for Identity Provider: " + name);
                }
                break;
            case EntityTypes.JDBC_CONNECTION:
                if (bundle.getJdbcConnections().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for JDBC Connection: " + name);
                }
                break;
            case EntityTypes.STORED_PASSWORD_TYPE:
                if (bundle.getStoredPasswords().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for Password: " + name);
                }
                break;
            case EntityTypes.TRUSTED_CERT_TYPE:
                if (bundle.getTrustedCerts().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for Trusted Certificate: " + name);
                }
                break;
            case EntityTypes.PRIVATE_KEY_TYPE:
                if (bundle.getPrivateKeys().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for Private Key: " + name);
                }
                break;
            case EntityTypes.CASSANDRA_CONNECTION_TYPE:
                if (bundle.getCassandraConnections().get(name) == null) {
                    throw new MissingEnvironmentException("Missing environment value for Cassandra Connection: " + name);
                }
                break;
            default:
                throw new MissingEnvironmentException("Missing environment value for " + type + ": " + name);
        }
    }
}
