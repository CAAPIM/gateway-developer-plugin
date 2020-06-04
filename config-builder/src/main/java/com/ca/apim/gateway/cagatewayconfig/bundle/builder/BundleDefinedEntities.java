package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import java.util.Collection;

public class BundleDefinedEntities {

    private Collection<DefaultMetadata> definedEntities;

    public Collection<DefaultMetadata> getDefinedEntities() {
        return definedEntities;
    }

    public void setDefinedEntities(Collection<DefaultMetadata> definedEntities) {
        this.definedEntities = definedEntities;
    }

    public static class DefaultMetadata implements Metadata {
        private String name;
        private String type;
        private String id;
        private String guid;

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }
    }
}
