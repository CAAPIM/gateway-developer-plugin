package com.ca.apim.gateway.cagatewayconfig.beans.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BundleMetadata extends MetadataBase {
    private String id;
    private String version;
    private String description;
    private List<Metadata> definedEntities;

    private BundleMetadata(String type, String name, String version) {
        super(type, name);
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<Metadata> getDefinedEntities() {
        return definedEntities;
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final String type;
        private final String version;
        private String description;
        private List<Metadata> definedEntities = new LinkedList<>();
        private List<Metadata> environmentEntities = new LinkedList<>();
        private List<Metadata> dependencies = new LinkedList<>();

        public Builder(String type, String id, String name, String version) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.version = version;
        }

        public Builder definedEntities(final List<Metadata> entities) {
            definedEntities.clear();
            definedEntities.addAll(entities);
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public BundleMetadata build() {
            BundleMetadata bundleMetadata = new BundleMetadata(type, name, version);
            bundleMetadata.id = id;
            bundleMetadata.description = description;
            bundleMetadata.definedEntities = definedEntities;
            return bundleMetadata;
        }
    }
}
