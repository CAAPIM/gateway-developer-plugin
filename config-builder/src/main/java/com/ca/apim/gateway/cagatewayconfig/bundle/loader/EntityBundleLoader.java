/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.ITEM;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

@Singleton
public class EntityBundleLoader {
    private static final Logger LOGGER = Logger.getLogger(EntityBundleLoader.class.getName());

    private final DocumentTools documentTools;
    private final BundleEntityLoaderRegistry entityLoaderRegistry;
    private final JsonFileUtils jsonFileUtils;

    @Inject
    EntityBundleLoader(final DocumentTools documentTools, final BundleEntityLoaderRegistry entityLoaderRegistry, final JsonFileUtils jsonFileUtils) {
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.jsonFileUtils = jsonFileUtils;
    }

    public Bundle load(List<File> fileSet, BundleLoadingOperation loadingMode, String... entityTypes) {
        final Bundle bundle = new Bundle();
        fileSet.forEach(f -> loadBundleFile(f, bundle, loadingMode, entityTypes));
        return bundle;
    }

    public Bundle load(File dependencyBundlePath, BundleLoadingOperation loadingMode, String... entityTypes) {
        final Bundle bundle = new Bundle();

        loadBundleFile(dependencyBundlePath, bundle, loadingMode, entityTypes);

        return bundle;
    }

    public Bundle loadMetadata(File dependencyMetadataPath, BundleLoadingOperation loadingMode) {
        final Bundle bundle = new Bundle();
        bundle.setLoadingMode(loadingMode);
        BundleDefinedEntities bundleDefinedEntities = jsonFileUtils.readBundleMetadataFile(dependencyMetadataPath);
        Collection<DefaultMetadata> metadataCollection = bundleDefinedEntities.getDefinedEntities();
        if (metadataCollection != null) {
            metadataCollection.forEach(metadata -> {
                if (EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(metadata.getType())) {
                    Encass encass = getEncassFromMetadata(metadata);
                    Map<String, Encass> encassMap = bundle.getEncasses();
                    encassMap.put(encass.getName(), encass);
                } else if (EntityTypes.POLICY_TYPE.equals(metadata.getType())) {
                    Policy policy = getPolicyFromMetadata(metadata);
                    Map<String, Policy> policyMap = bundle.getPolicies();
                    policyMap.put(metadata.getName(), policy);
                }
            });
        }
        return bundle;
    }

    private static Policy getPolicyFromMetadata(final Metadata metadata) {
        Policy policy = new Policy();
        policy.setName(metadata.getName());
        policy.setId(metadata.getId());
        policy.setGuid(metadata.getGuid());
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(AnnotableEntity.SHARED_ANNOTATION);
        policy.setAnnotations(annotations);
        return policy;
    }

    private Encass getEncassFromMetadata(final Metadata metadata) {
        Encass encass = new Encass();
        encass.setName(metadata.getName());
        encass.setId(metadata.getId());
        encass.setGuid(metadata.getGuid());
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(AnnotableEntity.SHARED_ANNOTATION);
        encass.setAnnotations(annotations);
        return encass;
    }

    private void loadBundleFile(File dependencyBundlePath, Bundle bundle, BundleLoadingOperation loadingMode, String... entityTypes) {
        bundle.setLoadingMode(loadingMode);

        final Document bundleDocument;
        try {
            bundleDocument = documentTools.parse(dependencyBundlePath);
        } catch (DocumentParseException e) {
            throw new BundleLoadException("Could not parse dependency bundle '" + dependencyBundlePath + "': " + e.getMessage(), e);
        }

        final NodeList nodeList = bundleDocument.getElementsByTagName(ITEM);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                handleItem(bundle, (Element) node, entityTypes);
            }
        }
    }

    private void handleItem(Bundle bundle, final Element element, String[] entityTypes) {
        final String type = getSingleChildElement(element, TYPE).getTextContent();
        if (isNotEmpty(entityTypes) && !contains(entityTypes, type)) {
            return;
        }

        final BundleEntityLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            entityLoader.load(bundle, element);
        } else {
            LOGGER.log(Level.FINE, "No entity loader found for entity type: {0}", type);
        }
    }
}
