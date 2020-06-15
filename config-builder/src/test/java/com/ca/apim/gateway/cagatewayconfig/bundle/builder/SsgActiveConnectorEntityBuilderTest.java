package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SsgActiveConnectorEntityBuilderTest {

    @Test
    public void buildNoActiveConnectors() {
        final SsgActiveConnectorEntityBuilder builder = new SsgActiveConnectorEntityBuilder(new IdGenerator());
        final Bundle bundle = new Bundle();
        List<Entity> activeConnectors = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(0, activeConnectors.size());

        activeConnectors = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(0, activeConnectors.size());
    }

    @Test
    public void buildActiveConnectors() {
        final SsgActiveConnectorEntityBuilder builder = new SsgActiveConnectorEntityBuilder(new IdGenerator());


        List<Entity> activeConnectors = builder.build(getTestBundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, activeConnectors.size());
        Entity ssgActiveConnector = activeConnectors.get(0);
        assertEquals("testName", ssgActiveConnector.getName());
        Element element = ssgActiveConnector.getXml();
        assertEquals("testServiceId", DocumentUtils.getSingleChildElementTextContent(element, BundleElementNames.HARDWIRED));
        activeConnectors = builder.build(getTestAnnotatedBundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, activeConnectors.size());

    }

    private Bundle getTestAnnotatedBundle() {
        Bundle bundle = getTestBundle();
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, new AnnotatedEntity<>(new Encass()), new ProjectInfo("test", "testGroup", "1.0"));
        annotatedBundle.putAllSsgActiveConnectors(bundle.getSsgActiveConnectors());
        return annotatedBundle;
    }

    private Bundle getTestBundle() {
        final Bundle bundle = new Bundle();
        SsgActiveConnector ssgActiveConnector = new SsgActiveConnector();
        String name = "testName";
        String type = "MqNative";
        String enabled = "true";
        String serviceRef = "testService";
        Service service = new Service();
        service.setName(serviceRef);
        service.setId("testServiceId");
        Map<String, Service> serviceMap = bundle.getServices();
        serviceMap.put(serviceRef, service);
        ssgActiveConnector.setName(name);
        ssgActiveConnector.setType(type);
        ssgActiveConnector.setEnabled(enabled);
        ssgActiveConnector.setTargetServiceReference(serviceRef);
        Map<String, Object> properties = new HashMap<>();
        String passwordName = "testPassword";
        Map<String, StoredPassword> storedPasswordMap = bundle.getStoredPasswords();
        StoredPassword storedPassword = new StoredPassword();
        storedPassword.setName(passwordName);
        storedPassword.setId("testPasswordId");
        storedPasswordMap.put(passwordName, storedPassword);

        PrivateKey privateKey = new PrivateKey();
        String privateKeyName = "testPrivateKey";
        privateKey.setAlias(privateKeyName);
        privateKey.setId("testPrivateId:" + privateKeyName);
        Map<String, PrivateKey> privateKeyMap = bundle.getPrivateKeys();
        privateKeyMap.put(privateKeyName, privateKey);
        properties.put("MyNativeSecurePasswordOid", passwordName);
        properties.put("MqNativeSslKeystoreAlias", privateKeyName);
        ssgActiveConnector.setProperties(properties);
        Map<String, SsgActiveConnector> ssgActiveConnectorMap = bundle.getSsgActiveConnectors();
        ssgActiveConnectorMap.put(name, ssgActiveConnector);
        return bundle;
    }
}
