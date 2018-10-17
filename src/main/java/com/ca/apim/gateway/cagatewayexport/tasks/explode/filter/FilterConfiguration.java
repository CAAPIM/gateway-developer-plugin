package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import java.util.HashSet;
import java.util.Set;

public class FilterConfiguration {

    private final Set<String> clusterProperties = new HashSet<>();
    private final Set<String> identityProviders = new HashSet<>();
    private final Set<String> jdbcConnections = new HashSet<>();
    private final Set<String> listenPorts = new HashSet<>();
    private final Set<String> privateKeys = new HashSet<>();
    private final Set<String> passwords = new HashSet<>();
    private final Set<String> certificates = new HashSet<>();

    public Set<String> getClusterProperties() {
        return clusterProperties;
    }

    public Set<String> getIdentityProviders() {
        return identityProviders;
    }

    public Set<String> getJdbcConnections() {
        return jdbcConnections;
    }

    public Set<String> getListenPorts() {
        return listenPorts;
    }

    public Set<String> getPrivateKeys() {
        return privateKeys;
    }

    public Set<String> getPasswords() {
        return passwords;
    }

    public Set<String> getCertificates() {
        return certificates;
    }
}
