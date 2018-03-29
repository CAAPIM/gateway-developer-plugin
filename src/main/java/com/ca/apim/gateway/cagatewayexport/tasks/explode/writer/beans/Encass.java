/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import java.util.List;

public class Encass {
    private List<EncassParam> arguments;
    private List<EncassParam> results;

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
}
