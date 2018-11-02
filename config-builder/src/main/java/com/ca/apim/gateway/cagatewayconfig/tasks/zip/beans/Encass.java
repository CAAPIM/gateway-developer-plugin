/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Named("ENCAPSULATED_ASSERTION")
public class Encass extends GatewayEntity {
    private String policy;
    private Set<EncassParam> arguments;
    private Set<EncassParam> results;
    private String guid;
    private String policyId;
    private String path;

    public Set<EncassParam> getArguments() {
        return arguments;
    }

    public void setArguments(Set<EncassParam> arguments) {
        this.arguments = arguments;
    }

    public Set<EncassParam> getResults() {
        return results;
    }

    public void setResults(Set<EncassParam> results) {
        this.results = results;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
