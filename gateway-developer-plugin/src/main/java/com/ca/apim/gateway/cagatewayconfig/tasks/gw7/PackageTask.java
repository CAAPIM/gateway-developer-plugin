/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.util.bundle.DependencyBundlesProcessor;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionProvider;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.*;
import static org.apache.commons.collections4.SetUtils.union;

/**
 * The BuildBundle task will take local source files and create a bundle document that can be bootstrapped into a gateway container
 */
public class PackageTask extends DefaultTask {
    private static final Logger LOGGER = Logger.getLogger(PackageTask.class.getName());
    private ConfigurableFileCollection dependencyBundles;
    private ConfigurableFileCollection containerApplicationDependencies;
    private ConfigurableFileCollection dependencyModularAssertions;
    private ConfigurableFileCollection dependencyCustomAssertions;
    private RegularFileProperty into;
    private RegularFileProperty bundle;
    private DirectoryProperty bundleDirectory;

    private final FileUtils fileUtils;
    private final GW7Builder gw7Builder;
    private final DependencyBundlesProcessor dependencyBundlesProcessor;

    /**
     * Creates a new BuildBundle task to build a bundle from local source files
     */
    @Inject
    public PackageTask() {
        this(FileUtils.INSTANCE, GW7Builder.INSTANCE);
    }

    PackageTask(final FileUtils fileUtils, GW7Builder gw7Builder) {
        into = newOutputFile();
        bundle = newInputFile();
        bundleDirectory = newInputDirectory();
        dependencyBundles = getProject().files();
        containerApplicationDependencies = getProject().files();
        dependencyModularAssertions = getProject().files();
        dependencyCustomAssertions = getProject().files();

        this.fileUtils = fileUtils;
        this.gw7Builder = gw7Builder;
        this.dependencyBundlesProcessor = InjectionRegistry.getInstance(DependencyBundlesProcessor.class);
    }

    @InputFile
    public RegularFileProperty getBundle() {
        return bundle;
    }

    public DirectoryProperty getBundleDirectory(){
        return bundleDirectory;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @InputFiles
    public ConfigurableFileCollection getContainerApplicationDependencies() {
        return containerApplicationDependencies;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyModularAssertions() {
        return dependencyModularAssertions;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyCustomAssertions() {
        return dependencyCustomAssertions;
    }

    @OutputFile
    public RegularFileProperty getInto() {
        return into;
    }

    @TaskAction
    public void perform() {
        Packager packager = new Packager(fileUtils, gw7Builder, dependencyBundlesProcessor);
        final Set<File> bundleDependencies = dependencyBundles.getAsFileTree().getFiles();
        FileTree fileTree = bundleDirectory.getAsFileTree();
        Set<File> files = fileTree.getFiles();
        LOGGER.log(Level.WARNING, "built bundles " + files);
        files.forEach(file -> {
            File out = into.getAsFile().get();
            File outDirectory = out.getParentFile();
            LOGGER.log(Level.WARNING, "outDirectory" + outDirectory.getPath());
            File artifact = new File(outDirectory, file.getName() + ".gw7");
            LOGGER.log(Level.WARNING, "output file path " + artifact.getPath());
            packager.buildPackage(artifact,
                    file,
                    filterBundleFiles(bundleDependencies),
                    containerApplicationDependencies.getFiles(),
                    union(dependencyModularAssertions.getFiles(), filterModularAssertionFiles(bundleDependencies)),
                    union(dependencyCustomAssertions.getFiles(), filterJarFiles(bundleDependencies))
            );
        });
    }
}
