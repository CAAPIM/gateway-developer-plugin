/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.StringTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.bundle.DependencyBundlesProcessor;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtilsException;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.SystemUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.copyNodes;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

/**
 * This combines the environment bundle generation with the deployment bundle generation and outputs one single full bundle
 * with everything on it. This is used to output the whole solution in one bundle and allow to be installed into non ephemeral gateways.
 */
@Singleton
public class FullBundleCreator {

    private static final Logger LOGGER = Logger.getLogger(FullBundleCreator.class.getName());

    private final DocumentTools documentTools;
    private final EnvironmentBundleBuilder environmentBundleBuilder;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final FileUtils fileUtils;
    private final DependencyBundlesProcessor dependencyBundlesProcessor;
    private final EntityLoaderRegistry entityLoaderRegistry;

    @Inject
    FullBundleCreator(DocumentTools documentTools,
                      EnvironmentBundleBuilder environmentBundleBuilder,
                      BundleEntityBuilder bundleEntityBuilder,
                      FileUtils fileUtils, DependencyBundlesProcessor dependencyBundlesProcessor, EntityLoaderRegistry entityLoaderRegistry) {
        this.documentTools = documentTools;
        this.environmentBundleBuilder = environmentBundleBuilder;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.fileUtils = fileUtils;
        this.dependencyBundlesProcessor = dependencyBundlesProcessor;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    public void createFullBundle(File rootDir, Map<String, String> environmentProperties,
                                 List<File> deploymentBundles,
                                 String bundleFolderPath,
                                 String bundleFileName,
                                 boolean detemplatizeDeploymentBundles) {
        final Map<String, String> bundles = createFullBundleAsString(rootDir, environmentProperties, deploymentBundles, detemplatizeDeploymentBundles);
        Set<Map.Entry<String, String>> entrySet =  bundles.entrySet();
        for(Map.Entry<String, String> entry: entrySet) {
            // write the full bundle to a temporary file first
            final File fullBundleFile = new File(System.getProperty(JAVA_IO_TMPDIR), entry.getKey() + "-full.bundle");
            try {
                writeStringToFile(fullBundleFile, entry.getValue(), defaultCharset());
            } catch (IOException e) {
                throw new DocumentFileUtilsException("Error writing to file '" + bundleFileName + "': " + e.getMessage(), e);
            }

            // process for reattaching loose encasses and write to the final path
            dependencyBundlesProcessor.process(singletonList(fullBundleFile), bundleFolderPath);
            // delete the temp file
            boolean deleted = fullBundleFile.delete();
            if (!deleted) {
                LOGGER.log(Level.WARNING, "Temporary bundle file was not deleted: " + fullBundleFile.toString());
            }
        }
    }

    private Map<String, String> createFullBundleAsString(File rootDir, Map<String, String> environmentProperties,
                                            List<File> deploymentBundles,
                                            boolean detemplatizeDeploymentBundles) {
        // load all deployment bundles to strings
        List<TemplatizedBundle> templatizedBundles = deploymentBundles.stream().map(f -> new StringTemplatizedBundle(f.getName(), fileUtils.getFileAsString(f))).collect(toList());

        // generate the environment one
        Bundle environmentBundle = new Bundle();
        environmentBundleBuilder.build(environmentBundle, environmentProperties, EMPTY, PLUGIN);
        if (rootDir != null) {
            // Load the entities to build a deployment bundle
            final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
            entityLoaders.parallelStream().forEach(e -> e.load(environmentBundle, rootDir));

            // create the folder tree
            FolderLoaderUtils.createFolders(environmentBundle, rootDir, environmentBundle.getServices());
        }

        // validate and detemplatize
        processDeploymentBundles(environmentBundle, templatizedBundles, PLUGIN, detemplatizeDeploymentBundles);

        // generate the environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        Map<String, Element> bundleElements = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        Map<String, String> fullBundleStrings = new HashMap<>();
        Set<Map.Entry<String, Element>> entrySet =  bundleElements.entrySet();
        for(Map.Entry<String, Element> entry: entrySet) {
            // generate the environment bundle
            Element bundleElement = entry.getValue();
            Element referencesElement = getSingleChildElement(bundleElement, REFERENCES);
            Element mappingsElement = getSingleChildElement(bundleElement, MAPPINGS);
            List<Element> items = DocumentUtils.getChildElements(referencesElement, ITEM);
            Set<String> environmentItems = items.stream().map(e -> buildBundleItemKey(e)).collect(Collectors.toSet());
            List<Element> mappings = DocumentUtils.getChildElements(mappingsElement, MAPPING);
            Set<String> environmentMappings = mappings.stream().map(e -> buildBundleMappingKey(e)).collect(Collectors.toSet());
            // store Set of elements previously added so avoiding repetition in the resulting bundle
            Set<String> addedItems = new HashSet<>();
            Set<String> addedMappings = new HashSet<>();

            // merge the deployment bundles into the environment one to get the full bundle
            templatizedBundles.forEach(tb -> {
                LOGGER.log(Level.WARNING, "bundle name {0}, env name {1} ", new String[]{ tb.getName(), entry.getKey()});
                if(tb.getName().equals(entry.getKey() + ".bundle")){
                    try {
                        final Element detemplatizedBundleElement = documentTools.parse(tb.getContents()).getDocumentElement();
                        copyNodes(getSingleChildElement(detemplatizedBundleElement, REFERENCES), ITEM, document, referencesElement, item -> {
                            final String key = buildBundleItemKey(item);
                            return !environmentItems.contains(key) && addedItems.add(key);
                        });
                        copyNodes(getSingleChildElement(detemplatizedBundleElement, MAPPINGS), MAPPING, document, mappingsElement, mapping -> {
                            final String key = buildBundleMappingKey(mapping);
                            return !environmentMappings.contains(key) && addedItems.contains(key) && addedMappings.add(key);
                        });
                    } catch (DocumentParseException e) {
                        throw new EntityBuilderException("Unable to read bundle " + tb.getName(), e);
                    }
                }
            });
            fullBundleStrings.put(entry.getKey(), documentTools.elementToString(bundleElement));
        }

        return fullBundleStrings;
    }
}
