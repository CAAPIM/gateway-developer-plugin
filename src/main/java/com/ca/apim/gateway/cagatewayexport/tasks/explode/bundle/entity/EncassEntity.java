/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import java.util.List;

public class EncassEntity implements Entity {
    private final String name;
    private final String id;
    private final String guid;
    private final String policyId;
    private final List<EncassParam> arguments;
    private final List<EncassParam> results;
    private String path;

    public EncassEntity(final String name, final String id, final String guid, String policyId, List<EncassParam> arguments, List<EncassParam> results) {
        this.name = name;
        this.id = id;
        this.guid = guid;
        this.policyId = policyId;
        this.arguments = arguments;
        this.results = results;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPolicyId() {
        return policyId;
    }

    public List<EncassParam> getArguments() {
        return arguments;
    }

    public List<EncassParam> getResults() {
        return results;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static class EncassParam {
        private String name;
        private String type;

        public EncassParam(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
