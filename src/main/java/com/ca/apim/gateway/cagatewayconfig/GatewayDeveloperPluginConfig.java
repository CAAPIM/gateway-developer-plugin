package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public class GatewayDeveloperPluginConfig {
    private final DirectoryProperty solutionDir;
    private RegularFileProperty builtBundle;

    public GatewayDeveloperPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        builtBundle = project.getLayout().fileProperty();
    }

    public DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    public RegularFileProperty getBuiltBundle() {
        return builtBundle;
    }
}
