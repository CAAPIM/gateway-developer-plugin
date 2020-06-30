/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.DependentBundle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collection;
import java.util.LinkedList;

@JsonPropertyOrder({"metaVersion", "name", "groupName", "moduleName", "version", "type", "tags", "description", "l7Template",
        "redeployable", "hasRouting", "definedEntities", "referencedEntities", "dependencies"})
public class BundleMetadata implements Metadata {
    @SuppressWarnings({"unused", "java:S1170"}) // Suppress IntelliJ warnings for this field
    private final String metaVersion = "1.0";
    private final String type;
    private final String name;
    private final String version;
    private final String moduleName;
    private final String groupName;
    private String description;
    private Collection<Metadata> definedEntities;
    private Collection<String> tags;
    private boolean l7Template;
    private boolean redeployable;
    private boolean hasRouting;
    private Collection<Metadata> referencedEntities;
    private Collection<DependentBundle> dependencies;

    private BundleMetadata(String type, String name, String moduleName, String groupName, String version) {
        this.type = type;
        this.name = name;
        this.moduleName = moduleName;
        this.groupName = groupName;
        this.version = version;
    }

    public String getMetaVersion() {
        return metaVersion;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getId() {
        return null;
    }

    @JsonIgnore
    public String getGuid(){
        return null;
    }

    public String getVersion() {
        return version;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public Collection<Metadata> getDefinedEntities() {
        return definedEntities;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public boolean isL7Template() {
        return l7Template;
    }

    public boolean isRedeployable() {
        return redeployable;
    }

    public boolean isHasRouting() {
        return hasRouting;
    }

    public Collection<Metadata> getReferencedEntities() {
        return referencedEntities;
    }

    public Collection<DependentBundle> getDependencies() {
        return dependencies;
    }

    public static class Builder {
        private final String name;
        private final String type;
        private String moduleName;
        private String groupName;
        private final String version;
        private String description;
        private boolean l7Template;
        private boolean redeployable;
        private boolean hasRouting;
        private Collection<String> tags;
        private Collection<Metadata> definedEntities = new LinkedList<>();
        private Collection<Metadata> referencedEntities = new LinkedList<>();
        private Collection<DependentBundle> dependencies = new LinkedList<>();

        public Builder(String type, String name, String moduleName, String groupName, String version) {
            this.type = type;
            this.name = name;
            this.moduleName = moduleName;
            this.groupName = groupName;
            this.version = version;
        }

        public Builder definedEntities(final Collection<Metadata> entities) {
            definedEntities.clear();
            definedEntities.addAll(entities);
            return this;
        }

        public Builder referencedEntities(final Collection<Metadata> referencedEntities) {
            this.referencedEntities.clear();
            this.referencedEntities.addAll(referencedEntities);
            return this;
        }

        public Builder dependencies(final Collection<DependentBundle> dependencies) {
            this.dependencies.clear();
            this.dependencies.addAll(dependencies);
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder redeployable(boolean redeployable) {
            this.redeployable = redeployable;
            return this;
        }

        public Builder l7Template(boolean l7Template) {
            this.l7Template = l7Template;
            return this;
        }

        public Builder hasRouting(boolean hasRouting) {
            this.hasRouting = hasRouting;
            return this;
        }

        public Builder tags(final Collection<String> tags) {
            this.tags = tags;
            return this;
        }

        public BundleMetadata build() {
            BundleMetadata bundleMetadata = new BundleMetadata(type, name, moduleName, groupName, version);
            bundleMetadata.description = description;
            bundleMetadata.definedEntities = definedEntities;
            bundleMetadata.l7Template = l7Template;
            bundleMetadata.redeployable = redeployable;
            bundleMetadata.hasRouting = hasRouting;
            bundleMetadata.tags = tags;
            bundleMetadata.referencedEntities = referencedEntities;
            bundleMetadata.dependencies = dependencies;
            return bundleMetadata;
        }
    }
}
