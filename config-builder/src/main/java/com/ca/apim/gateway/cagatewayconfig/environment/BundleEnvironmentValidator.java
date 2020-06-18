/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

class BundleEnvironmentValidator {
    private static final Logger LOGGER = Logger.getLogger(BundleEnvironmentValidator.class.getName());
    private final Bundle environmentBundle;
    private DocumentTools documentTools = DocumentTools.INSTANCE;

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
                throw new DeploymentBundleException("Expected mapping to be map by name: " + documentTools.elementToString(mapping));
            }
            List<Element> mapToProperties = propertyElements.stream().filter(p -> MAP_TO.equals(p.getAttribute(ATTRIBUTE_KEY))).collect(Collectors.toList());
            String mapToName = getSingleChildElementTextContent(mapToProperties.get(0), STRING_VALUE);

            String type = mapping.getAttribute(ATTRIBUTE_TYPE);
            if (mode.isRequired(type)) {
                findInBundle(environmentBundle, type, mapToName);
            }
        }
    }

    private void findInBundle(Bundle bundle, String type, String name) {
        Object entity = null;
        switch (type) {
            case EntityTypes.CLUSTER_PROPERTY_TYPE:
                entity = bundle.getGlobalEnvironmentProperties().get(PREFIX_GATEWAY + name);
                if (entity == null) {
                    throw new MissingEnvironmentException("Missing global environment value for property: " + name);
                }
                break;
            case EntityTypes.ID_PROVIDER_CONFIG_TYPE:
                entity = bundle.getIdentityProviders().get(name);
                break;
            case EntityTypes.JDBC_CONNECTION:
                entity = bundle.getJdbcConnections().get(name);
                break;
            case EntityTypes.STORED_PASSWORD_TYPE:
                entity = bundle.getStoredPasswords().get(name);
                break;
            case EntityTypes.TRUSTED_CERT_TYPE:
                entity = bundle.getTrustedCerts().get(name);
                break;
            case EntityTypes.PRIVATE_KEY_TYPE:
                entity = bundle.getPrivateKeys().get(name);
                break;
            case EntityTypes.CASSANDRA_CONNECTION_TYPE:
                entity = bundle.getCassandraConnections().get(name);
                break;
            case EntityTypes.JMS_DESTINATION_TYPE:
                entity = bundle.getJmsDestinations().get(name);
                break;
            case EntityTypes.SSG_ACTIVE_CONNECTOR:
                entity = bundle.getSsgActiveConnectors().get(name);
                break;
            case EntityTypes.GENERIC_TYPE:
                entity = bundle.getGenericEntities().get(name);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unsupported gateway entity " + type);
                UnsupportedGatewayEntity unsupportedGatewayEntity = bundle.getUnsupportedEntities().get(name);
                if (unsupportedGatewayEntity != null && type.equals(unsupportedGatewayEntity.getType())) {
                    entity = unsupportedGatewayEntity;
                }

        }

        if (entity == null) {
            throw new MissingEnvironmentException("Missing environment value for " + type + ": " + name);
        }
    }
}
