package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.SsgActiveConnector;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class SsgActiveConnectorLoader implements BundleEntityLoader {

    @VisibleForTesting
    public SsgActiveConnectorLoader() {
        //
    }

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element activeConnectorElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), ACTIVE_CONNECTOR);

        final String name = getSingleChildElementTextContent(activeConnectorElement, NAME);
        final String type = getSingleChildElementTextContent(activeConnectorElement, TYPE);
        final String enabled = getSingleChildElementTextContent(activeConnectorElement, ENABLED);
        final String targetServiceReference = getSingleChildElementTextContent(activeConnectorElement, HARDWIRED);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(activeConnectorElement, PROPERTIES, true), PROPERTIES);

        SsgActiveConnector ssgActiveConnector = new SsgActiveConnector();
        ssgActiveConnector.setName(name);
        ssgActiveConnector.setConnectorType(type);
        ssgActiveConnector.setProperties(properties);
        ssgActiveConnector.setTargetServiceReference(targetServiceReference);
        ssgActiveConnector.setId(activeConnectorElement.getAttribute(ATTRIBUTE_ID));
        Set<Annotation> annotations = new HashSet<>();
        Annotation bundleEntity = new Annotation(AnnotationType.BUNDLE_HINTS);
        bundleEntity.setId(activeConnectorElement.getAttribute(ATTRIBUTE_ID));
        annotations.add(bundleEntity);
        ssgActiveConnector.setAnnotations(annotations);
        //remove keystore ID
        Optional<Map.Entry<String, Object>> entryOptional = properties.entrySet().stream().filter(entry -> entry.getKey().endsWith("SslKeystoreId")).findFirst();
        entryOptional.ifPresent(entry -> {
            properties.remove(entry.getKey());
        });

        bundle.getSsgActiveConnectors().put(name, ssgActiveConnector);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.SSG_ACTIVE_CONNECTOR;
    }
}
