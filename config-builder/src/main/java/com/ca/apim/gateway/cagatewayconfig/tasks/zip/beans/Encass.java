/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.List;

public class Encass {
    private List<EncassParam> arguments;
    private List<EncassParam> results;
    private String guid;

    public List<EncassParam> getArguments() {
        return arguments;
    }

    public void setArguments(List<EncassParam> arguments) {
        this.arguments = arguments;
    }

    public List<EncassParam> getResults() {
        return results;
    }

    public void setResults(List<EncassParam> results) {
        this.results = results;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
