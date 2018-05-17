/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.export;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class BuildExportQueryTask extends DefaultTask {
    //Outputs
    private Property<String> exportQuery;

    public BuildExportQueryTask() {
        exportQuery = getProject().getObjects().property(String.class);
    }

    /**
     * The path of the folder to export.
     *
     * @return The generated export query
     */
    @Internal
    public Property<String> getExportQuery() {
        return exportQuery;
    }

    @TaskAction
    public void perform() {
        exportQuery.set("?encassAsPolicyDependency=true" +
                "&includeDependencies=true" +
                "&all=true");
    }
}
