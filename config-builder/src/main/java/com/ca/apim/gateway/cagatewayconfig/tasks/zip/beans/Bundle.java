/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Bundle {
    private final Map<String, Service> services = new HashMap<>();
    private final Map<String, Encass> encasses = new HashMap<>();
    private final Map<String, Policy> policies = new HashMap<>();
    private final Map<String, Folder> folders = new ConcurrentHashMap<>();
    private final Map<String, String> staticProperties = new HashMap<>();
    private final Map<String, String> environmentProperties = new HashMap<>();
    private final Map<String, PolicyBackedService> policyBackedServices = new HashMap<>();
    private final Map<String, IdentityProvider> identityProviders = new HashMap<>();
    private final Map<String, ListenPort> listenPorts = new HashMap<>();
    private final Map<String, StoredPassword> storedPasswords = new HashMap<>();
    private final Map<String, JdbcConnection> jdbcConnections = new HashMap<>();
    private final Map<String, TrustedCert> trustedCerts = new HashMap<>();
    private final Map<String, PrivateKey> privateKeys = new HashMap<>();
    private final Map<String, CassandraConnection> cassandraConnections = new HashMap<>();
    private final Map<String, SupplierWithIO<InputStream>> certificateFiles = new HashMap<>();
    private Set<Bundle> dependencies;

    public Map<String, Service> getServices() {
        return services;
    }

    public void putAllServices(@NotNull Map<String, Service> services) {
        this.services.putAll(services);
    }

    public Map<String, Policy> getPolicies() {
        return policies;
    }

    public synchronized void putAllPolicies(@NotNull Map<String, Policy> policies) {
        // Some loaders will partially load a policy entity
        // and the main loader will fully load,
        // so we merge the information in order to get the complete policy entity
        policies.forEach((path, p) -> this.policies.merge(path, p, Policy::merge));
    }

    public void putAllFolders(@NotNull Map<String, Folder> folders) {
        this.folders.putAll(folders);
    }

    public Map<String, Folder> getFolders() {
        return folders;
    }

    public void putAllEncasses(@NotNull Map<String, Encass> encasses) {
        this.encasses.putAll(encasses);
    }

    public Map<String, Encass> getEncasses() {
        return encasses;
    }

    public void putAllStaticProperties(@NotNull Map<String, String> properties) {
        this.staticProperties.putAll(properties);
    }

    public Map<String, String> getStaticProperties() {
        return staticProperties;
    }

    public void putAllEnvironmentProperties(@NotNull Map<String, String> properties) {
        this.environmentProperties.putAll(properties);
    }

    public Map<String, String> getEnvironmentProperties() {
        return environmentProperties;
    }

    public void putAllPolicyBackedServices(@NotNull Map<String, PolicyBackedService> policyBackedServices) {
        this.policyBackedServices.putAll(policyBackedServices);
    }

    public Map<String, PolicyBackedService> getPolicyBackedServices() {
        return policyBackedServices;
    }

    public Map<String, IdentityProvider> getIdentityProviders() {
        return identityProviders;
    }

    public void putAllIdentityProviders(@NotNull Map<String, IdentityProvider> identityProviders) {
        this.identityProviders.putAll(identityProviders);
    }

    public Map<String, ListenPort> getListenPorts() {
        return listenPorts;
    }

    public void putAllListenPorts(@NotNull Map<String, ListenPort> listenPorts) {
        this.listenPorts.putAll(listenPorts);
    }

    public Map<String, StoredPassword> getStoredPasswords() {
        return storedPasswords;
    }

    public void putAllStoredPasswords(@NotNull Map<String, StoredPassword> storedPasswords) {
        this.storedPasswords.putAll(storedPasswords);
    }

    public Map<String, JdbcConnection> getJdbcConnections() {
        return jdbcConnections;
    }

    public void putAllJdbcConnections(@NotNull Map<String, JdbcConnection> jdbcConnections) {
        this.jdbcConnections.putAll(jdbcConnections);
    }

    public Set<Bundle> getDependencies() {
        return dependencies;
    }

    public void setDependencies(@NotNull Set<Bundle> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, TrustedCert> getTrustedCerts() {
        return trustedCerts;
    }

    public void putAllTrustedCerts(@NotNull Map<String, TrustedCert> trustedCerts) {
        this.trustedCerts.putAll(trustedCerts);
    }

    public Map<String, PrivateKey> getPrivateKeys() {
        return privateKeys;
    }

    public void putAllPrivateKeys(@NotNull Map<String, PrivateKey> privateKeys) {
        this.privateKeys.putAll(privateKeys);
    }

    public Map<String, CassandraConnection> getCassandraConnections() {
        return cassandraConnections;
    }

    public void putAllCassandraConnections(@NotNull Map<String, CassandraConnection> cassandraConnections) {
        this.cassandraConnections.putAll(cassandraConnections);
    }

    public Map<String, SupplierWithIO<InputStream>> getCertificateFiles() {
        return certificateFiles;
    }

    public void putAllCertificateFiles(@NotNull Map<String, SupplierWithIO<InputStream>> certificateFiles) {
        this.certificateFiles.putAll(certificateFiles);
    }
}
