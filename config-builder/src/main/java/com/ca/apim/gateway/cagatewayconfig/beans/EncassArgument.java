/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

public class EncassArgument {

    private String name;
    private String type;
    private Boolean requireExplicit;

    public EncassArgument() {
    }

    public EncassArgument(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public EncassArgument(String name, String type, Boolean requireExplicit) {
        this(name, type);
        this.requireExplicit = requireExplicit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRequireExplicit() {
        return requireExplicit;
    }

    public void setRequireExplicit(Boolean requireExplicit) {
        this.requireExplicit = requireExplicit;
    }
}