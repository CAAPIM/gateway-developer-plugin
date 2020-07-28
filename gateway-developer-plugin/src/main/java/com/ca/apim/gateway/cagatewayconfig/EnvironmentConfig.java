package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import java.util.Map;

public class EnvironmentConfig {
    private final DirectoryProperty includeFolder;
    private final Property<String> name;
    private final Property<Map> map;

    public EnvironmentConfig(Project project) {
        includeFolder = project.getLayout().directoryProperty();
        map = project.getObjects().property(Map.class);
        name = project.getObjects().property(String.class);
    }


    Property<Map> getMap() {
        return map;
    }


    DirectoryProperty getIncludeFolder() {
        return includeFolder;
    }

    Property<String> getName() {
        return name;
    }
}
