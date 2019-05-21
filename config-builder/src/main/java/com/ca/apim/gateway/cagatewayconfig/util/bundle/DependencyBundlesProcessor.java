/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.bundle;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingMode;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.LinkedList;
import java.util.Spliterator;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyEntityBuilder.POLICY;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyEntityBuilder.resolvePossibleMissingEncapsulatedAssertionDependencies;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.ENCAPSULATED;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.stream.StreamSupport.stream;

/**
 * Processor for dependency bundles that apply necessary changes prior to packaging.
 */
@Singleton
public class DependencyBundlesProcessor {

    private final EntityBundleLoader entityBundleLoader;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    @Inject
    public DependencyBundlesProcessor(EntityBundleLoader entityBundleLoader, DocumentTools documentTools, DocumentFileUtils documentFileUtils) {
        this.entityBundleLoader = entityBundleLoader;
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
    }

    public LinkedList<File> process(final LinkedList<File> bundles, String bundleFolderPath) {
        Bundle bundleObject = entityBundleLoader.load(bundles, BundleLoadingMode.PERMISSIVE, ENCAPSULATED_ASSERTION_TYPE);
        LinkedList<File> processedBundles = new LinkedList<>();

        for (File bundle : bundles) {
            Document document = parseBundleFile(bundle);
            Element bundleElement = document.getDocumentElement();

            NodeList items = bundleElement.getElementsByTagName(ITEM);
            processEncasses(bundleObject, items);

            File processedBundle = writeProcessedBundle(bundle, bundleElement, bundleFolderPath);
            processedBundles.add(processedBundle);
        }

        return processedBundles;
    }

    @NotNull
    private File writeProcessedBundle(File bundle, Element document, String bundleFolderPath) {
        File processedBundle = new File(new File(bundleFolderPath), bundle.getName());
        processedBundle.deleteOnExit();

        documentFileUtils.createFile(document, processedBundle.toPath());
        return processedBundle;
    }

    private Document parseBundleFile(File bundle) {
        try {
            return documentTools.parse(bundle);
        } catch (DocumentParseException e) {
            throw new BundleLoadException(e.getMessage(), e);
        }
    }

    private void processEncasses(Bundle bundleObject, NodeList items) {
        stream(nodeList(items).spliterator(), false)
                .map(node -> (Element) node)
                .filter(element -> EntityTypes.POLICY_TYPE.equals(getSingleChildElementTextContent(element, TYPE)))
                .forEach(policyItem -> processPolicyItem(bundleObject, policyItem));
    }

    private void processPolicyItem(Bundle bundleObject, Element policyItem) {
        Spliterator<Node> resources = nodeList(policyItem.getElementsByTagName(RESOURCE)).spliterator();
        stream(resources, false)
                .map(node -> (Element) node)
                .filter(element -> POLICY.equals(element.getAttribute(ATTRIBUTE_TYPE)))
                .forEach(policyResource -> processPolicyDocument(policyResource, bundleObject));
    }

    private void processPolicyDocument(Element policyResource, Bundle bundleObject) {
        String policyXML = policyResource.getTextContent();
        Document policyDoc;
        try {
            policyDoc = stringToXMLDocument(documentTools, policyXML);
        } catch (DocumentParseException e) {
            throw new BundleLoadException(e.getMessage(), e);
        }

        nodeList(policyDoc.getDocumentElement().getElementsByTagName(ENCAPSULATED)).forEach(node -> resolvePossibleMissingEncapsulatedAssertionDependencies(bundleObject, (Element) node));
        policyResource.setTextContent(documentTools.elementToString(policyDoc.getDocumentElement()));
    }
}
