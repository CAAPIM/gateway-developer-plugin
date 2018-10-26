/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.Set;

public class Encass {
    private Set<EncassParam> arguments;
    private Set<EncassParam> results;
    private String guid;

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
}
