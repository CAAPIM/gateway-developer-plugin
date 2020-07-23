/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.*;
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.StringTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.bundle.DependencyBundlesProcessor;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtilsException;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ListUtils.union;
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
    private final DocumentFileUtils documentFileUtils;
    private final JsonFileUtils jsonFileUtils;

    @Inject
    FullBundleCreator(DocumentTools documentTools,
                      EnvironmentBundleBuilder environmentBundleBuilder,
                      BundleEntityBuilder bundleEntityBuilder,
                      FileUtils fileUtils, DependencyBundlesProcessor dependencyBundlesProcessor,
                      DocumentFileUtils documentFileUtils, JsonFileUtils jsonFileUtils) {
        this.documentTools = documentTools;
        this.environmentBundleBuilder = environmentBundleBuilder;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.fileUtils = fileUtils;
        this.dependencyBundlesProcessor = dependencyBundlesProcessor;
        this.documentFileUtils = documentFileUtils;
        this.jsonFileUtils = jsonFileUtils;
    }

    public void createFullBundle(final Pair<String, Map<String, String>> bundleEnvironmentValues, final List<File> dependentBundles,
                                 String bundleFolderPath, ProjectInfo projectInfo,
                                 String fullInstallBundleFilename, String environmentConfigurationFolderPath,
                                 boolean detemplatizeDeploymentBundles) {
        final Pair<Element, Element> elementPair = createFullAndDeleteBundles(bundleEnvironmentValues,
                dependentBundles, bundleFolderPath, environmentConfigurationFolderPath, detemplatizeDeploymentBundles, projectInfo);
        final String bundle = documentTools.elementToString(elementPair.getLeft());
        // write the full bundle to a temporary file first
        final File fullBundleFile = new File(System.getProperty(JAVA_IO_TMPDIR), fullInstallBundleFilename);
        try {
            writeStringToFile(fullBundleFile, bundle, defaultCharset());
        } catch (IOException e) {
            throw new DocumentFileUtilsException("Error writing to file '" + fullInstallBundleFilename + "': " + e.getMessage(), e);
        }

        // process for reattaching loose encasses and write to the final path
        dependencyBundlesProcessor.process(singletonList(fullBundleFile), bundleFolderPath);

        final String fullDeleteBundleFilename = fullInstallBundleFilename.replace(INSTALL_BUNDLE_EXTENSION, DELETE_BUNDLE_EXTENSION);
        documentFileUtils.createFile(elementPair.getRight(), new File(bundleFolderPath, fullDeleteBundleFilename).toPath());
        // delete the temp file
        boolean deleted = fullBundleFile.delete();
        if (!deleted) {
            LOGGER.log(Level.WARNING, () -> "Temporary bundle file was not deleted: " + fullBundleFile.toString());
        }

        // remove environment bundle from metadata's dependencies section
        Map<String, Object> bundleMetadata = jsonFileUtils.readBundleMetadataFile(bundleFolderPath, bundleEnvironmentValues.getLeft());
        if (bundleMetadata != null) {
            List<Map<String, String>> dependencies = ((List) bundleMetadata.get("dependencies"));
            dependencies.removeIf(new Predicate<Map<String, String>>() {
                @Override
                public boolean test(Map<String, String> dependentBundleMap) {
                    return dependentBundleMap.get("name").equals(projectInfo.getName() + "-" + PREFIX_ENVIRONMENT) && dependentBundleMap.get("groupName").equals(projectInfo.getGroupName()) &&
                            dependentBundleMap.get("version").equals(projectInfo.getMajorVersion() + "." + projectInfo.getMinorVersion());
                }
            });

            //clean up intermediate file
            cleanIntermediateFile(bundleFolderPath, bundleEnvironmentValues.getLeft() + JsonFileUtils.METADATA_FILE_NAME_SUFFIX);

            String bundleMetaFileName = bundleEnvironmentValues.getLeft();
            if (StringUtils.isNotBlank(projectInfo.getVersion())) {
                bundleMetadata.put("version", projectInfo.getVersion() + PREFIX_FULL);
                bundleMetaFileName = bundleMetaFileName + PREFIX_FULL;
            }
            //generated metadata file
            jsonFileUtils.createBundleMetadataFile(bundleMetadata, bundleMetaFileName, new File(bundleFolderPath));

        }
    }

    /**
     * Removes intermediate file generated during deployment bundle task
     * @param bundleFolderPath build folder
     * @param fileName file name
     */
    private void cleanIntermediateFile(final String bundleFolderPath, final String fileName) {
        final File intermediateFile = new File(bundleFolderPath, fileName);
        boolean deleted = intermediateFile.delete();
        if (!deleted) {
            LOGGER.log(Level.WARNING, () -> "intermediate file was not deleted: " + intermediateFile.toString());
        }
    }

    private Pair<Element, Element> createFullAndDeleteBundles(final Pair<String, Map<String, String>> bundleEnvironmentValues, final List<File> dependentBundles,
                                                              String bundleFolderPath,
                                                              String environmentConfigurationFolderPath,
                                                              boolean detemplatizeDeploymentBundles, ProjectInfo projectInfo) {
        final Map<String, String> environmentProperties = bundleEnvironmentValues.getRight();
        final List<File> deploymentBundles = collectFiles(bundleFolderPath,
                bundleEnvironmentValues.getLeft() + INSTALL_BUNDLE_EXTENSION);
        final List<File> deploymentDeleteBundle = collectFiles(bundleFolderPath,
                bundleEnvironmentValues.getLeft() + DELETE_BUNDLE_EXTENSION);
        final List<File> bundleFiles = union(deploymentBundles, dependentBundles);

        // load all deployment bundles to strings
        List<TemplatizedBundle> templatizedBundles = bundleFiles.stream().map(f -> new StringTemplatizedBundle(f.getName(), fileUtils.getFileAsString(f))).collect(toList());

        // generate the environment one
        Bundle environmentBundle = new Bundle(projectInfo);
        environmentBundleBuilder.build(environmentBundle, environmentProperties, environmentConfigurationFolderPath, PLUGIN);

        // validate and detemplatize
        processDeploymentBundles(environmentBundle, templatizedBundles, PLUGIN, detemplatizeDeploymentBundles);

        // generate the environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        Map<String, BundleArtifacts> bundleElements = bundleEntityBuilder.build(environmentBundle,
                EntityBuilder.BundleType.ENVIRONMENT, document, projectInfo);
        Element bundleElement = createFullBundleElement(bundleElements, templatizedBundles, document);
        Element deleteBundleElement = createDeleteBundleElement(bundleElements, deploymentDeleteBundle, dependentBundles, document);


        return ImmutablePair.of(bundleElement, deleteBundleElement);
    }

    private Element createFullBundleElement(final Map<String, BundleArtifacts> bundleElements, final List<TemplatizedBundle> templatizedBundles, final Document document) {
        Element bundleElement = null;
        for (Map.Entry<String, BundleArtifacts> entry : bundleElements.entrySet()) {
            // generate the environment bundle
            bundleElement = entry.getValue().getBundle();
            Element referencesElement = getSingleChildElement(bundleElement, REFERENCES);
            Element mappingsElement = getSingleChildElement(bundleElement, MAPPINGS);

            // store Set of elements previously added so avoiding repetition in the resulting bundle
            Set<String> addedItems = new HashSet<>();
            Set<String> addedMappings = new HashSet<>();

            // merge the deployment bundles into the environment one to get the full bundle
            templatizedBundles.forEach(tb -> {
                try {
                    final Element detemplatizedBundleElement = documentTools.parse(tb.getContents()).getDocumentElement();
                    copyNodes(getSingleChildElement(detemplatizedBundleElement, REFERENCES), ITEM, document, referencesElement, item -> addedItems.add(buildBundleItemKey(item)));
                    copyNodes(getSingleChildElement(detemplatizedBundleElement, MAPPINGS), MAPPING, document, mappingsElement, mapping -> {
                        final String key = buildBundleMappingKey(mapping);
                        return addedItems.contains(key) && addedMappings.add(key);
                    });
                } catch (DocumentParseException e) {
                    throw new EntityBuilderException("Unable to read bundle " + tb.getName(), e);
                }
            });
        }
        return bundleElement;
    }

    private Element createDeleteBundleElement(final Map<String, BundleArtifacts> bundleElements, final List<File> deploymentDeleteBundles, final List<File> dependentBundles, final Document document) {
        Element bundleElement = null;
        for (Map.Entry<String, BundleArtifacts> entry : bundleElements.entrySet()) {
            bundleElement = entry.getValue().getDeleteBundle();
            Element referencesElement = getSingleChildElement(bundleElement, REFERENCES);
            Element mappingsElement = getSingleChildElement(bundleElement, MAPPINGS);

            // store Set of elements previously added so avoiding repetition in the resulting bundle
            Set<String> addedItems = new HashSet<>();
            Set<String> addedMappings = new HashSet<>();

            // merge the dependent bundle mappings into the environment delete bundle one to get the full delete bundle
            dependentBundles.forEach(file -> {
                try {
                    final Element element = documentTools.parse(fileUtils.getFileAsString(file)).getDocumentElement();
                    copyNodes(getSingleChildElement(element, REFERENCES), ITEM, document, referencesElement, item -> addedItems.add(buildBundleItemKey(item)));
                    copyDeleteMappings(getSingleChildElement(element, MAPPINGS), MAPPING, document, mappingsElement, mapping -> {
                        final String key = buildBundleMappingKey(mapping);
                        final String type = mapping.getAttribute(ATTRIBUTE_TYPE);
                        return !EntityTypes.FOLDER_TYPE.equals(type) && addedItems.contains(key) && addedMappings.add(key);
                    });
                } catch (DocumentParseException e) {
                    throw new EntityBuilderException("Unable to read bundle " + file.getName(), e);
                }
            });

            //merge deployment delete bundle mappings
            addDeleteBundleNodes(deploymentDeleteBundles, referencesElement, mappingsElement, document);
        }
        return bundleElement;
    }

    private static void copyDeleteMappings(Element from, String nodeName, Document destination, Element appendInto, @Nullable Predicate<Element> approvingFunction) {
        List<Element> elementList = getChildElements(from, nodeName);
        for (int index = elementList.size() - 1; index >= 0; index--) {
            Element child = elementList.get(index);
            child.setAttribute(ATTRIBUTE_ACTION, MappingActions.DELETE);
            if (approvingFunction == null || approvingFunction.test(child)) {
                final Node cloned = child.cloneNode(true);
                destination.adoptNode(cloned);
                appendInto.appendChild(cloned);
            }
        }
    }

    private void addDeleteBundleNodes(final List<File> deploymentDeleteBundles, Element referencesElement, Element mappingsElement, Document document) {
        deploymentDeleteBundles.forEach(file -> {
            try {
                final Element element = documentTools.parse(fileUtils.getFileAsString(file)).getDocumentElement();
                copyNodes(getSingleChildElement(element, REFERENCES), ITEM, document, referencesElement, item -> true);
                copyNodes(getSingleChildElement(element, MAPPINGS), MAPPING, document, mappingsElement, mapping -> true);
            } catch (DocumentParseException e) {
                throw new EntityBuilderException("Unable to read bundle " + file.getName(), e);
            }
        });
    }
}