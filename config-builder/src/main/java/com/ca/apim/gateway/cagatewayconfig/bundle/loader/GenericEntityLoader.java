package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class GenericEntityLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {

        final Element genericEntityEle = getSingleChildElement(getSingleChildElement(element, RESOURCE), GENERIC_ENTITY);

        final String name = getSingleChildElementTextContent(genericEntityEle, NAME);
        final String description = getSingleChildElementTextContent(genericEntityEle, DESCRIPTION);
        final String entityClassName = getSingleChildElementTextContent(genericEntityEle, ENTITY_CLASS_NAME);
        final String valueXml = getSingleChildElementTextContent(genericEntityEle, VALUE_XML);

        GenericEntity genericEntity = new GenericEntity();
        genericEntity.setId(genericEntityEle.getAttribute(ATTRIBUTE_ID));
        genericEntity.setName(name);
        genericEntity.setDescription(description);
        genericEntity.setEntityClassName(entityClassName);
        genericEntity.setValueXml(valueXml);

        Set<Annotation> annotations = new HashSet<>();
        Annotation bundleEntity = new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE_ENTITY);
        bundleEntity.setId(genericEntityEle.getAttribute(ATTRIBUTE_ID));
        annotations.add(bundleEntity);
        genericEntity.setAnnotations(annotations);

        bundle.getGenericEntities().put(name, genericEntity);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.GENERIC_TYPE;
    }
}
