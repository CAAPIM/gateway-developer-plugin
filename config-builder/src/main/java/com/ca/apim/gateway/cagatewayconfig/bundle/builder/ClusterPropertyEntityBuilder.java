/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.CLUSTER_PROPERTY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class ClusterPropertyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 500;
    private final IdGenerator idGenerator;

    @Inject
    ClusterPropertyEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    @Override
    public List<Entity> build(Map<Class, Map<String, GatewayEntity>> entityMap, AnnotatedEntity annotatedEntity, Bundle bundle, BundleType bundleType, Document document) {
        Map<String, GatewayEntity> globalProperties = entityMap.get(GlobalEnvironmentProperty.class);
        Map<String, GatewayEntity> clusterProperties = entityMap.get(ClusterProperty.class);
        return buildEntities(globalProperties, clusterProperties, bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, ?> globalProperties, Map<String, ?> clusterProperties, Bundle bundle, BundleType bundleType, Document document){
        Stream.Builder<Entity> streamBuilder = Stream.builder();
        switch (bundleType) {
            case DEPLOYMENT:
                clusterProperties.entrySet().stream().map(propertyEntry -> {
                        if (bundle.getGlobalEnvironmentProperties().containsKey(PREFIX_GATEWAY + propertyEntry.getKey())) {
                            throw new EntityBuilderException("The Cluster property: '" + propertyEntry.getKey() + "' is defined in both static.properties and env.properties");
                        }
                        return buildClusterPropertyEntity(propertyEntry.getKey(), (ClusterProperty)propertyEntry.getValue(), document);
                }).forEach(streamBuilder);
                globalProperties.entrySet().stream()
                        .filter(propertyEntry -> propertyEntry.getKey().startsWith(PREFIX_GATEWAY))
                        .map(propertyEntry ->
                                EntityBuilderHelper.getEntityWithOnlyMapping(CLUSTER_PROPERTY_TYPE, propertyEntry.getKey().substring(PREFIX_GATEWAY.length()), idGenerator.generate())
                        ).forEach(streamBuilder);
                break;
            case ENVIRONMENT:
                globalProperties.entrySet().stream()
                        .filter(propertyEntry -> propertyEntry.getKey().startsWith(PREFIX_GATEWAY))
                        .map(propertyEntry ->
                                buildClusterPropertyEntity(propertyEntry.getKey().substring(PREFIX_GATEWAY.length()), (GlobalEnvironmentProperty)propertyEntry.getValue(), document)
                        ).forEach(streamBuilder);
                break;
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
        return streamBuilder.build().collect(Collectors.toList());
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        Map<String, ClusterProperty> clusterPropertyMap = bundle.getStaticProperties();
        Map<String, GlobalEnvironmentProperty>  globalEnvironmentProperties = bundle.getGlobalEnvironmentProperties();
        return buildEntities(globalEnvironmentProperties, clusterPropertyMap, bundle, bundleType, document);
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }

    private Entity buildClusterPropertyEntity(String name, PropertiesEntity value, Document document) {
        String id = idGenerator.generate();
        return EntityBuilderHelper.getEntityWithNameMapping(CLUSTER_PROPERTY_TYPE, name, id, buildClusterPropertyElement(name, id, value.getValue(), document));
    }

    private static Element buildClusterPropertyElement(String name, String id, String value, Document document) {
        return buildClusterPropertyElement(name, id, value, document, Collections.emptyMap());
    }

    private static Element buildClusterPropertyElement(String name, String id, String value, Document document, Map<String, String> valueAttributes) {
        Element clusterPropertyElement = createElementWithAttribute(document, CLUSTER_PROPERTY, ATTRIBUTE_ID, id);

        clusterPropertyElement.appendChild(createElementWithTextContent(document, NAME, name));

        Element valueElement = createElementWithTextContent(document, VALUE, value);
        valueAttributes.forEach(valueElement::setAttribute);
        clusterPropertyElement.appendChild(valueElement);
        return clusterPropertyElement;
    }
}
