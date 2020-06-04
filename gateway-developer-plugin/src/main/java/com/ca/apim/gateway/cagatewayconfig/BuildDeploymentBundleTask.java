/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.DependentBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.artifacts.configurations.Configurations;
import org.gradle.api.tasks.*;

import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.filterBundleFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.BUNDLE_EXTENSION;

/**
 * The BuildDeploymentBundle task will take local source files and create a deployment bundle document that can be bootstrapped into a gateway container
 * or pushed via restman to an appliance gateway.
 */
public class BuildDeploymentBundleTask extends DefaultTask {

    private DirectoryProperty from;
    private DirectoryProperty into;
    private ConfigurableFileCollection dependencies;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public BuildDeploymentBundleTask() {
        into = newOutputDirectory();
        from = newInputDirectory();
        dependencies = getProject().files();
    }

    @InputDirectory
    @Optional
    public DirectoryProperty getFrom() {
        return from;
    }

    @OutputDirectory
    public DirectoryProperty getInto() {
        return into;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencies() {
        return dependencies;
    }

    @TaskAction
    public void perform() {
        BundleFileBuilder bundleFileBuilder = InjectionRegistry.getInjector().getInstance(BundleFileBuilder.class);
        final ProjectInfo projectInfo = new ProjectInfo(getProject().getName(), getProject().getGroup().toString(),
                getProject().getVersion().toString());
        final List<DependentBundle> dependentBundles = getDependentBundles(dependencies.getFiles());
        bundleFileBuilder.buildBundle(from.isPresent() ? from.getAsFile().get() : null, into.getAsFile().get(),
                dependentBundles, projectInfo);
    }

    private List<DependentBundle> getDependentBundles(Set<File> files) {
        final List<DependentBundle> dependentBundles = new ArrayList<>();
        ConfigurationContainer configurations = getProject().getConfigurations();
        Configuration configuration = configurations.getByName("bundle");
        DependencySet dependencySet = configuration.getDependencies();

        files.forEach(file -> {
            if (file.getName().endsWith(BUNDLE_EXTENSION) || file.getName().endsWith(JsonFileUtils.METADATA_FILE_NAME_SUFFIX)) {
                DependentBundle dependentBundle = new DependentBundle(file);
                dependentBundles.add(dependentBundle);
                dependencySet.forEach(dependency -> {
                    if (file.getName().startsWith(dependency.getName() + "-" + dependency.getVersion())) {
                        dependentBundle.setGroupName(dependency.getGroup());
                        dependentBundle.setName(dependency.getName());
                        dependentBundle.setVersion(dependency.getVersion());
                        dependentBundle.setType("bundle");
                    }
                });
            }
        });
        return dependentBundles;
    }
}
