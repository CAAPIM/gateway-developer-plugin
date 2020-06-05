/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Collection;
import java.util.LinkedList;

@JsonPropertyOrder({"metaVersion", "id", "name", "groupName", "version", "type", "tags", "description", "reusable",
        "redeployable", "hasRouting", "environmentIncluded", "definedEntities", "environmentEntities", "dependencies"})
public class BundleMetadata implements Metadata {
    @SuppressWarnings({"unused", "java:S1170"}) // Suppress IntelliJ warnings for this field
    private final String metaVersion = "1.0";
    private final String type;
    private final String name;
    private final String id;
    private final String version;
    private final String groupName;
    private String description;
    private Collection<Metadata> definedEntities;
    private Collection<String> tags;
    private boolean reusable;
    private boolean redeployable;
    private boolean hasRouting;
    private boolean environmentIncluded;
    private Collection<Metadata> environmentEntities;

    private BundleMetadata(String type, String id, String name, String groupName, String version) {
        this.id = id;
        this.type = type;
        this.name = name;
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

    public String getId() {
        return id;
    }

    @JsonIgnore
    public String getGuid(){
        return null;
    }
    public String getVersion() {
        return version;
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

    public boolean isReusable() {
        return reusable;
    }

    public boolean isRedeployable() {
        return redeployable;
    }

    public boolean isHasRouting() {
        return hasRouting;
    }

    public boolean isEnvironmentIncluded() {
        return environmentIncluded;
    }

    public Collection<Metadata> getEnvironmentEntities() {
        return environmentEntities;
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final String type;
        private String groupName;
        private final String version;
        private String description;
        private boolean reusable;
        private boolean redeployable;
        private boolean hasRouting;
        private boolean environmentIncluded;
        private Collection<String> tags;
        private Collection<Metadata> definedEntities = new LinkedList<>();
        private Collection<Metadata> environmentEntities = new LinkedList<>();
        private Collection<Metadata> dependencies = new LinkedList<>();

        public Builder(String type, String id, String name, String groupName, String version) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.groupName = groupName;
            this.version = version;
        }

        public Builder definedEntities(final Collection<Metadata> entities) {
            definedEntities.clear();
            definedEntities.addAll(entities);
            return this;
        }

        public Builder environmentEntities(final Collection<Metadata> environmentEntities) {
            this.environmentEntities.clear();
            this.environmentEntities.addAll(environmentEntities);
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder reusableAndRedeployable(boolean reusable, boolean redeployable) {
            this.reusable = reusable;
            this.redeployable = redeployable;
            return this;
        }

        public Builder hasRouting(boolean hasRouting) {
            this.hasRouting = hasRouting;
            return this;
        }

        public Builder environmentIncluded(boolean environmentIncluded) {
            this.environmentIncluded = environmentIncluded;
            return this;
        }

        public Builder tags(final Collection<String> tags) {
            this.tags = tags;
            return this;
        }

        public BundleMetadata build() {
            BundleMetadata bundleMetadata = new BundleMetadata(type, id, name, groupName, version);
            bundleMetadata.description = description;
            bundleMetadata.definedEntities = definedEntities;
            bundleMetadata.reusable = reusable;
            bundleMetadata.redeployable = redeployable;
            bundleMetadata.hasRouting = hasRouting;
            bundleMetadata.environmentIncluded = environmentIncluded;
            bundleMetadata.tags = tags;
            bundleMetadata.environmentEntities = environmentEntities;
            return bundleMetadata;
        }
    }
}
