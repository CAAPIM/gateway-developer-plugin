/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class PolicyLoader implements BundleDependencyLoader {

    private static final Logger LOGGER = Logger.getLogger(PolicyLoader.class.getName());
    private static final String SERVICE_OPERATION = "Service Operation";
    private static final String INCLUDE = "Include";
    private static final List<String> SUPPORTED_POLICY_TYPES = Arrays.asList(SERVICE_OPERATION, INCLUDE);

    @Override
    public void load(Bundle bundle, Element element) {
        final Element policyElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY);
        final String guid = policyElement.getAttribute(ATTRIBUTE_GUID);

        final Element policyDetails = getSingleChildElement(policyElement, POLICY_DETAIL);
        Element policyTypeElement = getSingleChildElement(policyDetails, POLICY_TYPE);
        if (!SUPPORTED_POLICY_TYPES.contains(policyTypeElement.getTextContent())) {
            LOGGER.log(Level.WARNING, "Skipping unsupported PolicyType: {0}", policyTypeElement.getTextContent());
            return;
        }

        final String id = policyDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = policyDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(policyDetails, NAME);
        final String name = nameElement.getTextContent();

        Folder parentFolder = getFolder(bundle, folderId);

        Policy policy = new Policy();
        policy.setPath(getPath(parentFolder, name));
        policy.setName(name);
        policy.setParentFolder(parentFolder);
        policy.setGuid(guid);
        policy.setId(id);

        bundle.getPolicies().put(policy.getPath(), policy);
    }

    private String getPath(Folder parentFolder, String name) {
        return Paths.get(parentFolder.getPath()).resolve(name + ".xml").toString();
    }

    private Folder getFolder(Bundle bundle, String folderId) {
        List<Folder> folderList = bundle.getFolders().values().stream().filter(f -> folderId.equals(f.getId())).collect(Collectors.toList());
        if (folderList.isEmpty()) {
            throw new DependencyBundleLoadException("Invalid dependency bundle. Could not find folder with id: " + folderId);
        } else if (folderList.size() > 1) {
            throw new DependencyBundleLoadException("Invalid dependency bundle. Found multiple folders with id: " + folderId);
        }
        return folderList.get(0);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.POLICY_TYPE;
    }
}