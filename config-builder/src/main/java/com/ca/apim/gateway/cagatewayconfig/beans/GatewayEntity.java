/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Metadata;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class GatewayEntity {

    @JsonIgnore
    private String id;
    @JsonIgnore
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the mapping value for this entity. It should usually be the name, in some cases can be overwritten
     */
    @JsonIgnore
    public String getMappingValue() {
        return getName();
    }

    /**
     * Override this method to run anything after each entity is loaded from config files.
     *
     * @param entityKey the key for the entity
     * @param bundle the bundle object
     * @param rootFolder the root folder location, null if loading from environment
     * @param idGenerator the id generator instance
     */
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        //
    }

    /**
     * Override this method to run anything before entities are written to config files.
     *
     * @param configFolder the config folder location
     * @param documentFileUtils instance of {@link DocumentFileUtils}
     */
    public void preWrite(File configFolder, DocumentFileUtils documentFileUtils) {
        //
    }

    @JsonIgnore
    public Metadata getMetadata() {
        return null;
    }

}
