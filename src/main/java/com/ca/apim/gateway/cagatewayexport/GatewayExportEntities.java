package com.ca.apim.gateway.cagatewayexport;

import org.gradle.api.Project;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public class GatewayExportEntities {
    private final SetProperty<String> clusterProperties;
    private final SetProperty<String> identityProviders;
    private final SetProperty<String> jdbcConnections;
    private final SetProperty<String> listenPorts;
    private final SetProperty<String> privateKeys;
    private final SetProperty<String> passwords;
    private final SetProperty<String> certificates;

    @Inject
    public GatewayExportEntities(Project project) {
        clusterProperties = project.getObjects().setProperty(String.class);
        identityProviders = project.getObjects().setProperty(String.class);
        jdbcConnections = project.getObjects().setProperty(String.class);
        listenPorts = project.getObjects().setProperty(String.class);
        privateKeys = project.getObjects().setProperty(String.class);
        passwords = project.getObjects().setProperty(String.class);
        certificates = project.getObjects().setProperty(String.class);
    }

    public SetProperty<String> getClusterProperties() {
        return clusterProperties;
    }

    public SetProperty<String> getIdentityProviders() {
        return identityProviders;
    }

    public SetProperty<String> getJdbcConnections() {
        return jdbcConnections;
    }

    public SetProperty<String> getListenPorts() {
        return listenPorts;
    }

    public SetProperty<String> getPrivateKeys() {
        return privateKeys;
    }

    public SetProperty<String> getPasswords() {
        return passwords;
    }

    public SetProperty<String> getCertificates() {
        return certificates;
    }

    public void setFrom(GatewayExportEntities gatewayExportEntities) {
        clusterProperties.addAll(gatewayExportEntities.getClusterProperties());
        identityProviders.addAll(gatewayExportEntities.getIdentityProviders());
        jdbcConnections.addAll(gatewayExportEntities.getJdbcConnections());
        listenPorts.addAll(gatewayExportEntities.getListenPorts());
        privateKeys.addAll(gatewayExportEntities.getPrivateKeys());
        passwords.addAll(gatewayExportEntities.getPasswords());
        certificates.addAll(gatewayExportEntities.getCertificates());
    }
}
