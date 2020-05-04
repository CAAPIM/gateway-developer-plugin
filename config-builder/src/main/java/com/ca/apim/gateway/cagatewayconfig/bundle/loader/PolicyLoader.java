/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.string.CharacterBlacklistUtil;
import com.google.common.base.Joiner;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.beans.PolicyType.GLOBAL;
import static com.ca.apim.gateway.cagatewayconfig.beans.PolicyType.INTERNAL;
import static com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingOperation.VALIDATE;
import static com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_SUBTAG;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_TAG;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Singleton
public class PolicyLoader implements BundleEntityLoader {

    private static final Logger LOGGER = Logger.getLogger(PolicyLoader.class.getName());

    @Override
    public void load(Bundle bundle, Element element) {
        final Element policyElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY);
        final String guid = policyElement.getAttribute(ATTRIBUTE_GUID);

        final Element policyDetails = getSingleChildElement(policyElement, POLICY_DETAIL);
        final String policyType = getSingleChildElementTextContent(policyDetails, POLICY_TYPE);
        final Map<String, Object> policyDetailProperties = mapPropertiesElements(getSingleChildElement(policyDetails, PROPERTIES, true), PROPERTIES);
        final String policyTag = (String) policyDetailProperties.get(PROPERTY_TAG);
        if (!PolicyType.isValidType(policyType, policyTag)) {
            LOGGER.log(Level.WARNING, () -> {
                if (policyTag != null) {
                    return String.format("Skipping unsupported PolicyType: %s, with tag %s", policyType, policyTag);
                }
                return String.format("Skipping unsupported PolicyType: %s", policyType);
            });
            return;
        }

        final String id = policyDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = policyDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(policyDetails, NAME);
        final String name = CharacterBlacklistUtil.filterAndReplace(nameElement.getTextContent());

        final Element resources = getSingleChildElement(policyElement, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String policyString = resource.getTextContent();
        final PolicyType type = PolicyType.fromType(policyType);
        final String policySubtag = (String) policyDetailProperties.get(PROPERTY_SUBTAG);

        Folder parentFolder = getFolder(bundle, folderId);

        Policy policy = type.createPolicyObject();
        policy.setPath(getPath(parentFolder, name));
        policy.setName(name);
        policy.setParentFolder(parentFolder);
        policy.setGuid(guid);
        policy.setId(id);
        policy.setTag(policyTag);
        policy.setSubtag(policySubtag);
        policy.setPolicyType(type);
        policy.setPolicyDocument(policyElement);
        policy.setPolicyXML(policyString);

        Map<String, Policy> bundlePolicies = bundle.getPolicies();

        if (bundle.getLoadingMode() == VALIDATE) {
            Policy policyWithSameName = bundle.getPolicies().values().stream().filter(p -> Objects.equals(policy.getName(), p.getName())).findAny().orElse(null);
            if (policyWithSameName != null) {
                throw new BundleLoadException("Duplicate policies found with name '" + policyWithSameName.getName() + "': " + Joiner.on(", ").join(Arrays.asList(policyWithSameName.getPath(), policy.getPath())));
            }
        }

        if (bundlePolicies.containsKey(policy.getPath())) {
            String duplicatePathName = handleDuplicatePathName(bundlePolicies, policy);
            policy.setName(duplicatePathName.substring(duplicatePathName.lastIndexOf('/') + 1));
            policy.setPath(duplicatePathName);
        }

        bundle.getPolicies().put(policy.getPath(), policy);
        if (type == GLOBAL) {
            bundle.getEntities(GlobalPolicy.class).put(policy.getPath(), (GlobalPolicy) policy);
        } else if (type == INTERNAL && firstNonNull(policyTag, EMPTY).startsWith("audit")) {
            bundle.getEntities(AuditPolicy.class).put(policy.getPath(), (AuditPolicy) policy);
        }
    }

    @Override
    public String getEntityType() {
        return EntityTypes.POLICY_TYPE;
    }
}