package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SsgActiveConnectorLinkerTest {

    @Test
    public void testLinker() {
        final Bundle bundle = new Bundle();
        SsgActiveConnector ssgActiveConnector = new SsgActiveConnector();
        String name = "testName";
        String type = "MqNative";
        String enabled = "true";
        bundle.getFolders().put(Folder.ROOT_FOLDER_NAME, Folder.ROOT_FOLDER);
        bundle.buildFolderTree();
        String serviceRef = "testService";
        String serviceId = "testServiceId";
        Service service = new Service();
        service.setName(serviceRef);
        service.setId(serviceId);
        service.setParentFolder(Folder.ROOT_FOLDER);
        Map<String, Service> serviceMap = bundle.getServices();
        serviceMap.put(serviceRef, service);
        ssgActiveConnector.setName(name);
        ssgActiveConnector.setConnectorType(type);
        ssgActiveConnector.setTargetServiceReference(serviceId);
        Map<String, Object> properties = new HashMap<>();
        String passwordName = "testPassword";
        String passwordId = "testPasswordId";
        Map<String, StoredPassword> storedPasswordMap = bundle.getStoredPasswords();
        StoredPassword storedPassword = new StoredPassword();
        storedPassword.setName(passwordName);
        storedPassword.setId(passwordId);
        storedPasswordMap.put(passwordName, storedPassword);

        properties.put("MyNativeSecurePasswordOid", passwordId);
        ssgActiveConnector.setProperties(properties);
        Map<String, SsgActiveConnector> ssgActiveConnectorMap = bundle.getSsgActiveConnectors();
        ssgActiveConnectorMap.put(name, ssgActiveConnector);

        SsgActiveConnectorLinker ssgActiveConnectorLinker = new SsgActiveConnectorLinker();
        Bundle targetBundle = new Bundle();
        ssgActiveConnectorLinker.link(ssgActiveConnector, bundle, targetBundle);
        SsgActiveConnector ssgActiveConnector1 = bundle.getSsgActiveConnectors().get(name);
        Assert.assertEquals(serviceRef, ssgActiveConnector1.getTargetServiceReference());
        Assert.assertEquals(passwordName, ssgActiveConnector1.getProperties().get("MyNativeSecurePasswordOid"));
    }
}
