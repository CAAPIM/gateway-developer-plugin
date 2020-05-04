/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class Dependency {
    private  String id;
    @JsonIgnore
    private  Class<? extends GatewayEntity> type;
    private  String name;
    private  String entityType;
    public Dependency(){
    }

    public Dependency(String name, String type) {
        this(null, null, name, type);
    }

    public Dependency(String id, Class<? extends GatewayEntity> type){
        this(id, type, null, null);
    }

    public Dependency(String id, Class<? extends GatewayEntity> type, String name, String entityType) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.entityType = entityType;
    }

    public String getId() {
        return id;
    }

    public Class<? extends GatewayEntity> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getEntityType(){
        return entityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(entityType, that.entityType) &&
                Objects.equals(type, that.type);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(Class<? extends GatewayEntity> type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, name, entityType);
    }

    @Override
    public String toString(){
        return id + ":" + name +":" + entityType;
    }
}
