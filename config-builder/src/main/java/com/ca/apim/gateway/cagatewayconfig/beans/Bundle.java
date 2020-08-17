/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingOperation;
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Bundle {

    private static final String DISABLE_ENVIRONMENT_ENTITY_UNIQUE_NAMING = "com.ca.apim.build.disableEnvironmentEntityUniqueNaming";

    // simple map of entities to avoid having to add here a new map for each entity
    private final Map<Class, Map<String, ?>> entities = new ConcurrentHashMap<>();

    // some special things need their own maps
    private final Map<String, SupplierWithIO<InputStream>> certificateFiles = new HashMap<>();
    private Set<Bundle> dependencies;
    private FolderTree folderTree;
    private Map<Dependency, List<Dependency>> dependencyMap;
    private BundleLoadingOperation loadingMode;
    private DependentBundle dependentBundleFrom;
    private List<DependentBundle> dependentBundles = new ArrayList<>();
    private ProjectInfo projectInfo;
    private static final String UNIQUE_NAME_SEPARATOR = "::";
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9._\\-]*$");

    public Bundle(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public Bundle(){}

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public void addDependentBundle(DependentBundle dependentBundle){
        dependentBundles.add(dependentBundle);
    }

    public List<DependentBundle> getDependentBundles() {
        return dependentBundles;
    }

    public DependentBundle getDependentBundleFrom() {
        return dependentBundleFrom;
    }

    public void setDependentBundleFrom(DependentBundle dependentBundleFrom) {
        this.dependentBundleFrom = dependentBundleFrom;
    }

    @SuppressWarnings("unchecked")
    public <E extends GatewayEntity> Map<String, E> getEntities(Class<E> entityType) {
        return (Map<String, E>) entities.computeIfAbsent(entityType, (Function<Class, Map<String, E>>) aClass -> new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, MissingGatewayEntity> getMissingEntities() {
        return getEntities(MissingGatewayEntity.class);
    }

    public Map<String, UnsupportedGatewayEntity> getUnsupportedEntities() {
        return getEntities(UnsupportedGatewayEntity.class);
    }

    @SuppressWarnings("unchecked")
    public <E extends GatewayEntity> void addEntity(E entity) {
        getEntities((Class<GatewayEntity>) entity.getClass()).put(entity.getId(), entity);
    }

    public Map<String, ClusterProperty> getClusterProperties() {
        return getEntities(ClusterProperty.class);
    }

    public Map<String, Service> getServices() {
        return getEntities(Service.class);
    }

    public void putAllServices(@NotNull Map<String, Service> services) {
        this.getServices().putAll(services);
    }

    public Map<String, Policy> getPolicies() {
        return getEntities(Policy.class);
    }

    public synchronized void putAllPolicies(@NotNull Map<String, Policy> policies) {
        // Some loaders will partially load a policy entity
        // and the main loader will fully load,
        // so we merge the information in order to get the complete policy entity
        final Map<String, Policy> policyMap = this.getPolicies();
        policies.forEach((path, p) -> policyMap.merge(path, p, Policy::merge));
    }

    public Map<String, SoapResource> getSoapResources() {
        return getEntities(SoapResource.class);
    }

    public void putAllSoapResources(@NotNull Map<String, SoapResource> wsdls) {
        this.getSoapResources().putAll(wsdls);
    }

    public void putAllFolders(@NotNull Map<String, Folder> folders) {
        this.getFolders().putAll(folders);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Folder> getFolders() {
        return (Map<String, Folder>) entities.computeIfAbsent(Folder.class, (Function<Class, Map<String, Folder>>) aClass -> new ConcurrentHashMap<>());
    }

    public void putAllEncasses(@NotNull Map<String, Encass> encasses) {
        this.getEncasses().putAll(encasses);
    }

    public Map<String, Encass> getEncasses() {
        return getEntities(Encass.class);
    }

    public void putAllStaticProperties(@NotNull Map<String, ClusterProperty> properties) {
        this.getEntities(ClusterProperty.class).putAll(properties);
    }

    public Map<String, ClusterProperty> getStaticProperties() {
        return getEntities(ClusterProperty.class);
    }

    public void putAllGlobalEnvironmentProperties(@NotNull Map<String, GlobalEnvironmentProperty> properties) {
        this.getEntities(GlobalEnvironmentProperty.class).putAll(properties);
    }

    public void putAllServiceEnvironmentProperties(@NotNull Map<String, ServiceEnvironmentProperty> properties) {
        this.getEntities(ServiceEnvironmentProperty.class).putAll(properties);
    }

    public void putAllContextVariableEnvironmentProperties(@NotNull Map<String, ContextVariableEnvironmentProperty> properties) {
        this.getEntities(ContextVariableEnvironmentProperty.class).putAll(properties);
    }

    public Map<String, GlobalEnvironmentProperty> getGlobalEnvironmentProperties() {
        return getEntities(GlobalEnvironmentProperty.class);
    }

    public Map<String, ServiceEnvironmentProperty> getServiceEnvironmentProperties() {
        return getEntities(ServiceEnvironmentProperty.class);
    }

    public Map<String, ContextVariableEnvironmentProperty> getContextVariableEnvironmentProperties() {
        return getEntities(ContextVariableEnvironmentProperty.class);
    }

    public void putAllPolicyBackedServices(@NotNull Map<String, PolicyBackedService> policyBackedServices) {
        this.getPolicyBackedServices().putAll(policyBackedServices);
    }

    public Map<String, PolicyBackedService> getPolicyBackedServices() {
        return getEntities(PolicyBackedService.class);
    }

    public Map<String, IdentityProvider> getIdentityProviders() {
        return getEntities(IdentityProvider.class);
    }

    public void putAllIdentityProviders(@NotNull Map<String, IdentityProvider> identityProviders) {
        this.getIdentityProviders().putAll(identityProviders);
    }

    public Map<String, ListenPort> getListenPorts() {
        return getEntities(ListenPort.class);
    }

    public void putAllListenPorts(@NotNull Map<String, ListenPort> listenPorts) {
        this.getEntities(ListenPort.class).putAll(listenPorts);
    }

    public Map<String, StoredPassword> getStoredPasswords() {
        return getEntities(StoredPassword.class);
    }

    public void putAllStoredPasswords(@NotNull Map<String, StoredPassword> storedPasswords) {
        this.getStoredPasswords().putAll(storedPasswords);
    }

    public Map<String, JdbcConnection> getJdbcConnections() {
        return getEntities(JdbcConnection.class);
    }

    public Map<String, SsgActiveConnector> getSsgActiveConnectors() {
        return getEntities(SsgActiveConnector.class);
    }

    public void putAllJdbcConnections(@NotNull Map<String, JdbcConnection> jdbcConnections) {
        this.getJdbcConnections().putAll(jdbcConnections);
    }

    public Set<Bundle> getDependencies() {
        return dependencies;
    }

    public void setDependencies(@NotNull Set<Bundle> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, TrustedCert> getTrustedCerts() {
        return getEntities(TrustedCert.class);
    }

    public void putAllTrustedCerts(@NotNull Map<String, TrustedCert> trustedCerts) {
        this.getTrustedCerts().putAll(trustedCerts);
    }

    public Map<String, PrivateKey> getPrivateKeys() {
        return getEntities(PrivateKey.class);
    }

    public void putAllPrivateKeys(@NotNull Map<String, PrivateKey> privateKeys) {
        this.getPrivateKeys().putAll(privateKeys);
    }

    public void putAllSsgActiveConnectors(@NotNull Map<String, SsgActiveConnector> ssgActiveConnectors) {
        this.getSsgActiveConnectors().putAll(ssgActiveConnectors);
    }

    public Map<String, CassandraConnection> getCassandraConnections() {
        return getEntities(CassandraConnection.class);
    }

    public void putAllCassandraConnections(@NotNull Map<String, CassandraConnection> cassandraConnections) {
        this.getCassandraConnections().putAll(cassandraConnections);
    }

    public Map<String, SupplierWithIO<InputStream>> getCertificateFiles() {
        return certificateFiles;
    }

    public void putAllCertificateFiles(@NotNull Map<String, SupplierWithIO<InputStream>> certificateFiles) {
        this.certificateFiles.putAll(certificateFiles);
    }

    public Map<String, ScheduledTask> getScheduledTasks() {
        return getEntities(ScheduledTask.class);
    }

    public void putAllScheduledTasks(@NotNull Map<String, ScheduledTask> scheduledTasks) {
        this.getScheduledTasks().putAll(scheduledTasks);
    }

    public Map<String, JmsDestination> getJmsDestinations() {
        return getEntities(JmsDestination.class);
    }
    
    public void putAllJmsDestinations(@NotNull Map<String, JmsDestination> jmsDestinations) {
        this.getJmsDestinations().putAll(jmsDestinations);
    }

    public Map<String, GenericEntity> getGenericEntities() {
        return getEntities(GenericEntity.class);
    }

    public FolderTree getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(FolderTree folderTree) {
        this.folderTree = folderTree;
    }

    public void buildFolderTree() {
        this.folderTree = new FolderTree(this.getFolders().values());
    }

    public Map<Dependency, List<Dependency>> getDependencyMap() {
        return dependencyMap;
    }

    public void setDependencyMap(Map<Dependency, List<Dependency>> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    public BundleLoadingOperation getLoadingMode() {
        return loadingMode;
    }

    public void setLoadingMode(BundleLoadingOperation loadingMode) {
        this.loadingMode = loadingMode;
    }

    public String getTargetFolderPath(){
        return projectInfo.getTargetFolderPath();
    }

    /**
     * Applies unique name space and version to the given entity name
     * ::namespace::entityName::majorVersion.minorVersion
     *
     * @param entityName String
     * @return String
     */
    public String applyUniqueName(final String entityName, final EntityBuilder.BundleType bundleType) {
        return applyUniqueName(entityName, bundleType, false);
    }

    /**
     * Applies unique name space and version to the given entity name
     * ::namespace::entityName::majorVersion.minorVersion
     * If the entity or entity's parent is Shared entity, "namespace" is just the Project group name.
     *
     * @param entityName Entity name
     * @param bundleType Bundle type
     * @param isShared   Is shared entity
     * @return Unique entity name
     */
    public String applyUniqueName(final String entityName, final EntityBuilder.BundleType bundleType,
                                  boolean isShared) {
        if (bundleType == EntityBuilder.BundleType.ENVIRONMENT && isEnvironmentEntityUniqueNamingDisabled()) {
            return entityName;
        }

        StringBuilder uniqueName = new StringBuilder(UNIQUE_NAME_SEPARATOR);
        uniqueName.append(getNamespace(bundleType, isShared));
        uniqueName.append(UNIQUE_NAME_SEPARATOR);
        uniqueName.append(entityName);

        String version = getProjectInfo().getVersion();
        if (StringUtils.isNotBlank(version)) {
            uniqueName.append(UNIQUE_NAME_SEPARATOR);
            String[] subVersions = version.split("\\.");
            uniqueName.append(subVersions.length > 0 ? subVersions[0] : version);
            uniqueName.append(".");
            uniqueName.append(subVersions.length > 1 ? subVersions[1] : "0");
        }

        return uniqueName.toString();
    }

    /**
     * Returns namespace for the entity for unique naming.
     *
     * For non-shared entities, it returns &lt;groupName&lt;.&gt;bundleName&gt;
     * For shared entities and non-annotated bundle, it returns just &lt;groupName&gt;
     *
     * @param bundleType bundle type
     * @param isShared is a shared entity (or is any parent entity is Shared)
     * @return namespace
     */
    public String getNamespace(final EntityBuilder.BundleType bundleType, boolean isShared) {
        return getProjectInfo().getGroupName();
    }

    public static boolean isEnvironmentEntityUniqueNamingDisabled() {
        return Boolean.getBoolean(DISABLE_ENVIRONMENT_ENTITY_UNIQUE_NAMING);
    }

}
