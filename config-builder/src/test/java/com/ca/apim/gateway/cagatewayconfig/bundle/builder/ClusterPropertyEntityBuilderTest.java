/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.GlobalEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.PropertiesEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertOnlyMappingEntity;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.testDeploymentBundleWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.CLUSTER_PROPERTY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.reverse;
import static org.junit.jupiter.api.Assertions.*;

class ClusterPropertyEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String ENV_PROP_1 = PREFIX_GATEWAY + "envprop1";
    private static final String ENV_PROP_2 = PREFIX_GATEWAY + "envprop2";
    private static final String STATIC_PROP_1 = "staticprop1";
    private static final String STATIC_PROP_2 = "staticprop2";
    private static final Map<String, GlobalEnvironmentProperty> ENV_PROPS = ImmutableMap.of(ENV_PROP_1, new GlobalEnvironmentProperty(ENV_PROP_1, reverse(ENV_PROP_1)), ENV_PROP_2, new GlobalEnvironmentProperty(ENV_PROP_2, reverse(ENV_PROP_2)));
    private static final Map<String, ClusterProperty> STATIC_PROPS = ImmutableMap.of(STATIC_PROP_1, new ClusterProperty(STATIC_PROP_1, reverse(STATIC_PROP_1)), STATIC_PROP_2, new ClusterProperty(STATIC_PROP_2, reverse(STATIC_PROP_2)));

    @Test
    void buildFromEmptyBundle_noProperties() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildDeploymentBundleWithOnlyEnvProps() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllGlobalEnvironmentProperties(ENV_PROPS);

        testDeploymentBundleWithOnlyMapping(
                builder,
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                CLUSTER_PROPERTY_TYPE, ENV_PROPS.keySet().stream().map(k -> k.replace(PREFIX_GATEWAY, "")).collect(toList())
        );
    }

    @Test
    void buildDeploymentBundleWithStaticProps() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllStaticProperties(STATIC_PROPS);

        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertExpectedProperties(entities, ImmutableMap.<String, PropertiesEntity>builder().putAll(STATIC_PROPS).build());
    }

    @Test
    void buildDeploymentBundleWithMixedProps() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllStaticProperties(STATIC_PROPS);
        bundle.putAllGlobalEnvironmentProperties(ENV_PROPS);

        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertExpectedProperties(entities, ImmutableMap.<String, PropertiesEntity>builder().putAll(STATIC_PROPS).putAll(ENV_PROPS).build());
    }

    @Test
    void buildEnvironmentBundleWithProps_ignoringStatic() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllStaticProperties(STATIC_PROPS);
        bundle.putAllGlobalEnvironmentProperties(ENV_PROPS);

        final List<Entity> entities = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertExpectedProperties(entities, ImmutableMap.<String, PropertiesEntity>builder().putAll(ENV_PROPS).build());
    }

    @Test
    void buildDeploymentBundleWithDuplicateProps() {
        ClusterPropertyEntityBuilder builder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();
        bundle.putAllStaticProperties(STATIC_PROPS);
        //Duplicate prop envprop1 in both static and env props
        bundle.getStaticProperties().put("envprop1", new ClusterProperty("envprop1", "some value"));
        bundle.putAllGlobalEnvironmentProperties(ENV_PROPS);

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    private static void assertExpectedProperties(List<Entity> entities, Map<String, PropertiesEntity> readOnlyExpectedProps) {
        Map<String, String> expectedProps = readOnlyExpectedProps.entrySet().stream().collect(toMap(o -> o.getKey().replace(PREFIX_GATEWAY, ""), e -> e.getValue().getValue()));
        assertFalse(entities.isEmpty());
        entities.forEach(e -> {
            assertNotNull(e.getId());
            assertEquals(CLUSTER_PROPERTY_TYPE, e.getType());
            if (e.getXml() == null) {
                assertOnlyMappingEntity(CLUSTER_PROPERTY_TYPE, new ArrayList<>(expectedProps.keySet()), e);
            } else {
                assertNotNull(expectedProps.get(e.getName()));

                Element xml = e.getXml();
                assertEquals(CLUSTER_PROPERTY, xml.getNodeName());
                assertNotNull(getSingleChildElement(xml, NAME));
                assertEquals(e.getName(), getSingleChildElementTextContent(xml, NAME));
                assertNotNull(getSingleChildElement(xml, VALUE));
                assertEquals(expectedProps.get(e.getName()), getSingleChildElementTextContent(xml, VALUE));
            }
            expectedProps.remove(e.getName());
        });
        assertTrue(expectedProps.isEmpty());
    }

}