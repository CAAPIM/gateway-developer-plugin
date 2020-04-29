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
    @JsonIgnore
    private String id;
    @JsonIgnore
    private Class<? extends GatewayEntity> typeClass;
    private String name;
    private String type;

    public Dependency() {
    }

    public Dependency(String id, Class<? extends GatewayEntity> typeClass) {
        this(id, typeClass, null, null);
    }

    public Dependency(String id, Class<? extends GatewayEntity> typeClass, String name, String type) {
        this.id = id;
        this.typeClass = typeClass;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Class<? extends GatewayEntity> getTypeClass() {
        return typeClass;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(typeClass, that.typeClass);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTypeClass(Class<? extends GatewayEntity> typeClass) {
        this.typeClass = typeClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, typeClass, name, type);
    }

    @Override
    public String toString() {
        return id + ":" + name + ":" + type;
    }
}
