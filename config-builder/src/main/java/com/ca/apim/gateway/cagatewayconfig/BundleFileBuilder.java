/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
class BundleFileBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final BundleCache cache;
    private final DocumentTools documentTools;

    private static final Logger LOGGER = Logger.getLogger(BundleFileBuilder.class.getName());

    @Inject
    BundleFileBuilder(final DocumentTools documentTools,
                      final DocumentFileUtils documentFileUtils,
                      final EntityLoaderRegistry entityLoaderRegistry,
                      final BundleEntityBuilder bundleEntityBuilder,
                      final BundleCache cache) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.cache = cache;
    }

    void buildBundle(File rootDir, File outputDir, List<File> dependencies, String name) {
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final Bundle bundle = new Bundle();

        if (rootDir != null) {
            // Load the entities to build a deployment bundle
            final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
            entityLoaders.parallelStream().forEach(e -> e.load(bundle, rootDir));

            // create the folder tree
            FolderLoaderUtils.createFolders(bundle, rootDir, bundle.getServices());

            //Load Dependencies
            final Set<Bundle> dependencyBundles = dependencies.stream().map(cache::getBundleFromFile).collect(Collectors.toSet());

            // Log overridden entities
            if (!dependencyBundles.isEmpty()) {
                logOverriddenEntities(bundle, dependencyBundles, Service.class);
                logOverriddenEntities(bundle, dependencyBundles, Policy.class);
            }

            bundle.setDependencies(dependencyBundles);
        }

        //Zip
        Element bundleElement = bundleEntityBuilder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, document);
        documentFileUtils.createFile(bundleElement, new File(outputDir, name + ".bundle").toPath());
    }

    private <E extends GatewayEntity> void logOverriddenEntities(Bundle bundle, Set<Bundle> dependencyBundles, Class<E> entity) {
        bundle.getEntities(entity).keySet().forEach(entityName -> {
            dependencyBundles.forEach(dependencyBundle -> {
                if (dependencyBundle.getEntities(entity).containsKey(entityName)) {
                    LOGGER.log(Level.INFO,"{0} policy will be overwritten by local version", entityName);
                }
            });
        });
    }


}
