package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class UnsupportedEntityBuilderTest {

    @Test
    public void testEnvironmentBuildForEmptyEntities() {
        UnsupportedEntityBuilder unsupportedEntityBuilder = new UnsupportedEntityBuilder();
        List<Entity> entities = unsupportedEntityBuilder.build(new Bundle(), EntityBuilder.BundleType.ENVIRONMENT, null);
        Assert.assertTrue(entities.isEmpty());

        entities = unsupportedEntityBuilder.build(new AnnotatedBundle(null, null, null), EntityBuilder.BundleType.DEPLOYMENT, null);
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void testDeploymentBuildForEmptyEntities() {
        UnsupportedEntityBuilder unsupportedEntityBuilder = new UnsupportedEntityBuilder();
        List<Entity> entities = unsupportedEntityBuilder.build(new Bundle(), EntityBuilder.BundleType.DEPLOYMENT, null);
        Assert.assertTrue(entities.isEmpty());

        entities = unsupportedEntityBuilder.build(new AnnotatedBundle(null, null, null), EntityBuilder.BundleType.DEPLOYMENT, null);
        Assert.assertTrue(entities.isEmpty());
    }

    @Test
    public void testDeployementBuild(){
        UnsupportedEntityBuilder unsupportedEntityBuilder = new UnsupportedEntityBuilder();
        Bundle bundle = new Bundle();
        String entityName = "TestEntity";
        Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap =  bundle.getUnsupportedEntities();
        UnsupportedGatewayEntity unsupportedGatewayEntity = new UnsupportedGatewayEntity();
        unsupportedGatewayEntity.setName(entityName);
        unsupportedGatewayEntity.setType("SSG_ACTIVE_CONNECTOR");
        unsupportedGatewayEntity.setId("testId");
        unsupportedGatewayEntityMap.put(entityName, unsupportedGatewayEntity);
        List<Entity> entities = unsupportedEntityBuilder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, null);
        Assert.assertEquals(1, entities.size());
        Entity entity = entities.get(0);
        Assert.assertNull(entity.getXml());
        Assert.assertEquals(entityName, entity.getName());

        AnnotatedBundle annotatedBundle = new AnnotatedBundle(null, null, null);
        unsupportedGatewayEntityMap =  annotatedBundle.getUnsupportedEntities();
        unsupportedGatewayEntityMap.put("TestEntity", unsupportedGatewayEntity);
        entities = unsupportedEntityBuilder.build(annotatedBundle, EntityBuilder.BundleType.DEPLOYMENT, null);
        Assert.assertEquals(1, entities.size());
        entity = entities.get(0);
        Assert.assertNull(entity.getXml());
        Assert.assertEquals(entityName, entity.getName());
    }

    @Test
    public void testEnvironmentBuild(){
        UnsupportedEntityBuilder unsupportedEntityBuilder = new UnsupportedEntityBuilder();
        Bundle bundle = new Bundle();
        String entityName = "TestEntity";
        String elementName = "TestElement";
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Element element = document.createElement(elementName);
        Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap =  bundle.getUnsupportedEntities();
        UnsupportedGatewayEntity unsupportedGatewayEntity = new UnsupportedGatewayEntity();
        unsupportedGatewayEntity.setName(entityName);
        unsupportedGatewayEntity.setType("SSG_ACTIVE_CONNECTOR");
        unsupportedGatewayEntity.setId("testId");
        unsupportedGatewayEntity.setElement(element);
        unsupportedGatewayEntityMap.put(entityName, unsupportedGatewayEntity);
        List<Entity> entities = unsupportedEntityBuilder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        Assert.assertEquals(1, entities.size());
        Entity entity = entities.get(0);
        Assert.assertNotNull(entity.getXml());
        Assert.assertEquals(entityName, entity.getName());
        Assert.assertEquals("SSG_ACTIVE_CONNECTOR", entity.getType());
        Assert.assertEquals(elementName, entity.getXml().getTagName());

        AnnotatedBundle annotatedBundle = new AnnotatedBundle(null, null, null);
        unsupportedGatewayEntityMap =  annotatedBundle.getUnsupportedEntities();
        unsupportedGatewayEntityMap.put("TestEntity", unsupportedGatewayEntity);
        entities = unsupportedEntityBuilder.build(annotatedBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        Assert.assertEquals(1, entities.size());
        entity = entities.get(0);
        Assert.assertNotNull(entity.getXml());
        Assert.assertEquals(entityName, entity.getName());
        Assert.assertEquals("SSG_ACTIVE_CONNECTOR", entity.getType());
        Assert.assertEquals(elementName, entity.getXml().getTagName());
    }
}
