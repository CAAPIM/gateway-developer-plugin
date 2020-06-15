package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.*;

public class SsgActiveConnectorFilterTest {
    @Test
    public void testFilter() {
        SsgActiveConnectorFilter ssgActiveConnectorFilter = new SsgActiveConnectorFilter();
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        Map<String, Collection<String>> entityFilters = new HashMap<>();
        Collection entityNames = new LinkedList();
        entityNames.add("testName");
        entityFilters.put("activeConnectors", entityNames);
        filterConfiguration.setEntityFilters(entityFilters);
        List<SsgActiveConnector> filteredEntities = ssgActiveConnectorFilter.filter("/", filterConfiguration, getTestBundle(), new Bundle());
        Assert.assertEquals(1, filteredEntities.size());

        entityFilters.put("activeConnectors", Collections.EMPTY_LIST);
        filterConfiguration.setEntityFilters(entityFilters);
        filteredEntities = ssgActiveConnectorFilter.filter("/", filterConfiguration, getTestBundle(), new Bundle());
        Assert.assertEquals(0, filteredEntities.size());
    }

    private Bundle getTestBundle() {
        Bundle bundle = new Bundle();
        bundle.setDependencyMap(new HashMap<>());
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
        ssgActiveConnector.setType(type);
        ssgActiveConnector.setEnabled(enabled);
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
        return bundle;
    }
}
