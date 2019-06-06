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
import com.ca.apim.gateway.cagatewayconfig.environment.TemplatizedBundle.StringTemplatizedBundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtilsException;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.copyNodes;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * This combines the environment bundle generation with the deployment bundle generation and outputs one single full bundle
 * with everything on it. This is used to output the whole solution in one bundle and allow to be installed into non ephemeral gateways.
 */
@Singleton
public class FullBundleCreator {

    private final DocumentTools documentTools;
    private final EnvironmentBundleBuilder environmentBundleBuilder;
    private final BundleEntityBuilder bundleEntityBuilder;
    private final FileUtils fileUtils;

    @Inject
    FullBundleCreator(DocumentTools documentTools,
                             EnvironmentBundleBuilder environmentBundleBuilder,
                             BundleEntityBuilder bundleEntityBuilder,
                             FileUtils fileUtils) {
        this.documentTools = documentTools;
        this.environmentBundleBuilder = environmentBundleBuilder;
        this.bundleEntityBuilder = bundleEntityBuilder;
        this.fileUtils = fileUtils;
    }

    public void createFullBundle(Map<String, String> environmentProperties,
                                 List<File> deploymentBundles,
                                 String bundleFolderPath,
                                 String bundleFileName,
                                 boolean detemplatizeDeploymentBundles) {
        final String bundle = createFullBundleAsString(environmentProperties, deploymentBundles, detemplatizeDeploymentBundles);
        try {
            writeStringToFile(new File(bundleFolderPath, bundleFileName), bundle, defaultCharset());
        } catch (IOException e) {
            throw new DocumentFileUtilsException("Error writing to file '" + bundleFileName + "': " + e.getMessage(), e);
        }
    }

    private String createFullBundleAsString(Map<String, String> environmentProperties,
                                            List<File> deploymentBundles,
                                            boolean detemplatizeDeploymentBundles) {
        // load all deployment bundles to strings
        List<TemplatizedBundle> templatizedBundles = deploymentBundles.stream().map(f -> new StringTemplatizedBundle(f.getName(), fileUtils.getFileAsString(f))).collect(toList());

        // generate the environment one
        Bundle environmentBundle = new Bundle();
        environmentBundleBuilder.build(environmentBundle, environmentProperties, EMPTY, PLUGIN);

        // validate and detemplatize
        processDeploymentBundles(environmentBundle, templatizedBundles, PLUGIN, detemplatizeDeploymentBundles);

        // generate the environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();
        Element bundleElement = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
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

        return documentTools.elementToString(bundleElement);
    }
}
