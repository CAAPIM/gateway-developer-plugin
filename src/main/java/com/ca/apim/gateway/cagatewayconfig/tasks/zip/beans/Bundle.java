/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Bundle {
    private final Map<String, Service> services = new HashMap<>();
    private final Map<String, Encass> encasses = new HashMap<>();
    private final Map<String, Policy> policies = new HashMap<>();
    private final Map<String, Folder> folders = new HashMap<>();
    private final Map<String, String> staticProperties = new HashMap<>();
    private final Map<String, String> environmentProperties = new HashMap<>();
    private final Map<String, PolicyBackedService> policyBackedServices = new HashMap<>();
    private final Map<String, ListenPort> listenPorts = new HashMap<>();
    private Set<Bundle> dependencies;

    public Map<String, Service> getServices() {
        return services;
    }

    public void putAllServices(Map<String, Service> services) {
        this.services.putAll(services);
    }

    public Map<String, Policy> getPolicies() {
        return policies;
    }

    public void putAllPolicies(Map<String, Policy> policies) {
        this.policies.putAll(policies);
    }

    public void putAllFolders(Map<String, Folder> folders) {
        this.folders.putAll(folders);
    }

    public Map<String, Folder> getFolders() {
        return folders;
    }

    public void putAllEncasses(Map<String, Encass> encasses) {
        this.encasses.putAll(encasses);
    }

    public Map<String, Encass> getEncasses() {
        return encasses;
    }

    public void putAllStaticProperties(Map<String, String> properties) {
        this.staticProperties.putAll(properties);
    }

    public Map<String, String> getStaticProperties() {
        return staticProperties;
    }

    public void putAllEnvironmentProperties(Map<String, String> properties) {
        this.environmentProperties.putAll(properties);
    }

    public Map<String, String> getEnvironmentProperties() {
        return environmentProperties;
    }

    public void putAllPolicyBackedServices(Map<String, PolicyBackedService> policyBackedServices) {
        this.policyBackedServices.putAll(policyBackedServices);
    }

    public Map<String, PolicyBackedService> getPolicyBackedServices() {
        return policyBackedServices;
    }

    public Map<String, ListenPort> getListenPorts() {
        return listenPorts;
    }

    public Set<Bundle> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Bundle> dependencies) {
        this.dependencies = dependencies;
    }
}
