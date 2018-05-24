/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.DependencyBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class BundleBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final DocumentTools documentTools;

    public BundleBuilder(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, final FileUtils fileUtils, final JsonTools jsonTools) {
        IdGenerator idGenerator = new IdGenerator();
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.entityLoaderRegistry = new EntityLoaderRegistry(fileUtils, jsonTools, idGenerator);
        this.bundleEntityBuilder = new BundleEntityBuilder(documentFileUtils, documentTools, document, idGenerator);
    }

    public void buildBundle(File rootDir, Path outputPath, Set<File> dependencies) {

        final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
        final Bundle bundle = new Bundle();

        //Load
        entityLoaders.parallelStream().forEach(e -> e.load(bundle, rootDir));

        //Load Dependencies
        // Improvements can be made here by doing this loading in a separate task and caching the intermediate results.
        // That way the dependent bundles are not re-processed on every new build
        final DependencyBundleLoader dependencyBundleLoader = new DependencyBundleLoader(documentTools);
        final Set<Bundle> dependencyBundles = dependencies.stream().map(dependencyBundleLoader::load).collect(Collectors.toSet());
        bundle.setDependencies(dependencyBundles);

        //Zip
        Element bundleElement = bundleEntityBuilder.build(bundle);
        documentFileUtils.createFile(bundleElement, outputPath);

    }


}
