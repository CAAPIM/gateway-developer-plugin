/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Generic Entity representation for the plugins.
 * It is used for non standard entities that need to have configuration saved to the database.
 */
@JsonInclude(NON_NULL)
@Named("GENERIC")
@ConfigurationFile(name = "generic-entities", type = JSON_YAML)
public class GenericEntity extends GatewayEntity {

    private String entityClassName;
    @JsonIgnore
    private String xml;

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    /**
     * Create key identifier for a generic entity based on class name + entity name
     *
     * @param entityClassName class name
     * @param name entity name
     * @return key composed by entityClassName_name
     */
    public static String createKey(String entityClassName, String name) {
        return entityClassName + "_" + name;
    }
}
