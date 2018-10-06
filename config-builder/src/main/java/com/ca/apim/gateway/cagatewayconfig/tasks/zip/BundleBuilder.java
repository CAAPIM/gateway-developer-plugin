/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.DependencyBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
class BundleBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final DependencyBundleLoader dependencyBundleLoader;
    private final DocumentTools documentTools;
    // FIXME remove when set keystore to be built in a proper place
    private final KeystoreHelper keystoreHelper;
    private final FileUtils fileUtils;

    @Inject
    BundleBuilder(final DocumentTools documentTools,
                  final DocumentFileUtils documentFileUtils,
                  final EntityLoaderRegistry entityLoaderRegistry,
                  final BundleEntityBuilder bundleEntityBuilder,
                  final DependencyBundleLoader dependencyBundleLoader,
                  final KeystoreHelper keystoreHelper,
                  final FileUtils fileUtils) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.dependencyBundleLoader = dependencyBundleLoader;
        this.keystoreHelper = keystoreHelper;
        this.fileUtils = fileUtils;
    }

    void buildBundle(File rootDir, File outputDir, Set<File> dependencies, String name) {
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final Bundle bundle = new Bundle();

        if (rootDir != null) {
            // Load the entities to build a deployment bundle
            final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
            entityLoaders.parallelStream().forEach(e -> e.load(bundle, rootDir));

            //Load Dependencies
            // Improvements can be made here by doing this loading in a separate task and caching the intermediate results.
            // That way the dependent bundles are not re-processed on every new build
            final Set<Bundle> dependencyBundles = dependencies.stream().map(dependencyBundleLoader::load).collect(Collectors.toSet());
            bundle.setDependencies(dependencyBundles);
        }

        //Zip
        Element bundleElement = bundleEntityBuilder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, document);
        documentFileUtils.createFile(bundleElement, new File(outputDir, name + ".req.bundle").toPath());

	    // Write Keystore
        final byte[] keyStore = keystoreHelper.createKeyStore(bundle.getPrivateKeys().values());
        fileUtils.writeContent(keyStore, new File(outputDir, "keystore.gwks"));
    }


}
