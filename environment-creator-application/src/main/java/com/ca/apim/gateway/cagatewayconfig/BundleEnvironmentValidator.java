package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class BundleEnvironmentValidator {

    private final Bundle environmentBundle;
    private DocumentTools documentTools = DocumentTools.INSTANCE;

    public BundleEnvironmentValidator(Bundle environmentBundle) {
        this.environmentBundle = environmentBundle;
    }

    /**
     * Validate that all required environment is available in the environment bundle
     *
     * @param bundleName       The name of the bundle to look for environment requirements in
     * @param deploymentBundle The deployment bundle to look for enviornment requirements in
     */
    public void validateEnvironmentProvided(String bundleName, String deploymentBundle) {
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
                List<Element> propertyElements = getChildElements(propertiesElement, PROPERTY);
                if (propertyElements.stream().anyMatch(p -> FAIL_ON_NEW.equals(p.getAttribute(ATTRIBUTE_KEY)) && Boolean.valueOf(getSingleChildElementTextContent(p, BOOLEAN_VALUE)))) {
                    boolean mapByName = propertyElements.stream().anyMatch(p -> MAP_BY.equals(p.getAttribute(ATTRIBUTE_KEY)) && MappingProperties.NAME.equals(getSingleChildElementTextContent(p, STRING_VALUE)));
                    if (!mapByName) {
                        throw new DeploymentBundleException("Expected mapping to be map by name: " + mapping.toString());
                    }
                    List<Element> mapToProperties = propertyElements.stream().filter(p -> MAP_TO.equals(p.getAttribute(ATTRIBUTE_KEY))).collect(Collectors.toList());
                    String mapToName = getSingleChildElementTextContent(mapToProperties.get(0), STRING_VALUE);
                    findInBundle(environmentBundle, mapping.getAttribute(ATTRIBUTE_TYPE), mapToName);
                }
            }
        });
    }

    private void findInBundle(Bundle bundle, String type, String name) {
        switch (type) {
            case EntityTypes.CLUSTER_PROPERTY_TYPE:
                if (bundle.getEnvironmentProperties().get(name) == null) {
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
            default:
                throw new MissingEnvironmentException("Missing environment value for " + type + ": " + name);
        }
    }
}
