/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.ANNOTATION_TYPE_EXCLUDE;

@Singleton
class BundleFileBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final JsonFileUtils jsonFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final BundleCache cache;
    private final DocumentTools documentTools;

    private static final Logger LOGGER = Logger.getLogger(BundleFileBuilder.class.getName());

    @Inject
    BundleFileBuilder(final DocumentTools documentTools,
                      final DocumentFileUtils documentFileUtils,
                      final JsonFileUtils jsonFileUtils,
                      final EntityLoaderRegistry entityLoaderRegistry,
                      final BundleEntityBuilder bundleEntityBuilder,
                      final BundleCache cache) {
        this.documentFileUtils = documentFileUtils;
        this.jsonFileUtils = jsonFileUtils;
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.cache = cache;
    }

    void buildBundle(File rootDir, File outputDir, List<File> dependencies, String projectName,
                     String projectGroupName, String projectVersion) {
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
        List<Pair<AnnotatedEntity, Encass>> annotatedEntities = new ArrayList<>();

        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        final Map<String, Encass> encassEntities = bundle.getEntities(Encass.class);
        final AnnotatedEntityCreator annotatedEntityCreator = AnnotatedEntityCreator.INSTANCE;
        encassEntities.entrySet().parallelStream().forEach(encassEntry -> {
            Encass encass = encassEntry.getValue();
            if (encass.getAnnotations() != null) {
                annotatedEntities.add(ImmutablePair.of(annotatedEntityCreator.createEntity(projectName, projectVersion, encass),
                        encass));
            }
        });
        final Map<String, Pair<Element, BundleMetadata>> bundles = new LinkedHashMap<>();
        if(!annotatedEntities.isEmpty()) {
            annotatedEntities.stream().forEach(annotatedEntityPair -> {
                if (annotatedEntityPair.getLeft().isBundleTypeEnabled()) {
                    final Pair<Element, BundleMetadata> bundleMetadataPair = bundleEntityBuilder.build(bundle,
                            EntityBuilder.BundleType.DEPLOYMENT, document, projectName, projectGroupName, projectVersion, annotatedEntityPair);
                    if(bundleMetadataPair != null){
                        bundles.put(annotatedEntityPair.getKey().getBundleName(), bundleMetadataPair);
                    }
                }
            });
        } else {
            bundles.put(projectName + '-' + projectVersion, bundleEntityBuilder.build(bundle,
                    EntityBuilder.BundleType.DEPLOYMENT, document, projectName, projectGroupName, projectVersion, null));
        }

        for (Map.Entry<String, Pair<Element, BundleMetadata>> entry : bundles.entrySet()) {
            Pair<Element, BundleMetadata> elementBundleMetadataPair = entry.getValue();
            if(elementBundleMetadataPair != null) {
                documentFileUtils.createFile(elementBundleMetadataPair.getLeft(),
                        new File(outputDir, entry.getKey() + ".bundle").toPath());
                jsonFileUtils.createBundleMetadataFile(entry.getValue().getRight(), entry.getKey(), outputDir);
            }
        }
    }

    protected <E extends GatewayEntity> void logOverriddenEntities(Bundle bundle, Set<Bundle> dependencyBundles, Class<E> entityClass) {
        bundle.getEntities(entityClass).keySet().forEach(entityName ->
            dependencyBundles.forEach(dependencyBundle -> {
                if (dependencyBundle.getEntities(entityClass).containsKey(entityName)) {
                    LOGGER.log(Level.INFO,"{0} policy will be overwritten by local version", entityName);
                }
            })
        );
    }


}
