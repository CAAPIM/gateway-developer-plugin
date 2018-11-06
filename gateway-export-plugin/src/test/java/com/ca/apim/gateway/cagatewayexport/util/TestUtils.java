/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.w3c.dom.Element;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TestUtils {

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString) {
        return createPolicy(name, id, guid, parentFolderId, policyElement, policyString, null);
    }

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString, String tag) {
        Policy policy = new Policy();
        policy.setName(name);
        policy.setId(id);
        policy.setGuid(guid);
        policy.setParentFolder(new Folder(parentFolderId, null));
        policy.setPolicyDocument(policyElement);
        policy.setPolicyXML(policyString);
        policy.setTag(tag);
        return policy;
    }

    public static CassandraConnection createCassandraConnection(String name, String id) {
        CassandraConnection cassandraConnection = new CassandraConnection();
        cassandraConnection.setName(name);
        cassandraConnection.setId(id);
        return cassandraConnection;
    }

    public static ClusterProperty createClusterProperty(String name, String value, String id) {
        ClusterProperty clusterProperty = new ClusterProperty();
        clusterProperty.setName(name);
        clusterProperty.setValue(value);
        clusterProperty.setId(id);
        return clusterProperty;
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId) {
        return createEncass(name, id, guid, policyId, null, null);
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId, Set<EncassArgument> arguments, Set<EncassResult> results) {
        Encass encass = new Encass();
        encass.setName(name);
        encass.setId(id);
        encass.setGuid(guid);
        encass.setPolicyId(policyId);
        encass.setArguments(arguments);
        encass.setResults(results);
        return encass;
    }

    public static Folder createFolder(final String name, final String id, final Folder parentFolder) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setId(id);
        folder.setParentFolder(parentFolder);
        return folder;
    }

    public static PolicyBackedService createPolicyBackedService(final String name, final String id, String interfaceName, final Map<String, String> operations) {
        PolicyBackedService pbs = new PolicyBackedService();
        pbs.setName(name);
        pbs.setId(id);
        pbs.setInterfaceName(interfaceName);
        pbs.setOperations(new LinkedHashSet<>());
        operations.forEach((k,v) -> pbs.getOperations().add(new PolicyBackedServiceOperation(k, v)));
        return pbs;
    }

    public static Service createService(final String name, final String id, final Folder parentFolder, Element serviceDetailsElement, String policy) {
        Service service = new Service();
        service.setName(name);
        service.setId(id);
        service.setParentFolder(parentFolder);
        service.setServiceDetailsElement(serviceDetailsElement);
        service.setPolicy(policy);
        return service;
    }

}
