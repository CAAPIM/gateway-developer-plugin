/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.isValidType;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_TAG;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class PolicyLoader implements EntityLoader<PolicyEntity> {
    private static final Logger LOGGER = Logger.getLogger(PolicyLoader.class.getName());

    @Override
    public PolicyEntity load(Element element) {
        final Element policy = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY);
        final String guid = policy.getAttribute(ATTRIBUTE_GUID);

        final Element policyDetails = getSingleChildElement(policy, POLICY_DETAIL);
        final String policyType = getSingleChildElementTextContent(policyDetails, POLICY_TYPE);
        final Map<String, Object> policyDetailProperties = mapPropertiesElements(getSingleChildElement(policyDetails, PROPERTIES), PROPERTIES);
        final String policyTag = (String) policyDetailProperties.get(PROPERTY_TAG);
        if (!isValidType(policyType, policyTag)) {
            LOGGER.log(Level.WARNING, () -> {
                if (policyTag != null) {
                    return String.format("Skipping unsupported PolicyType: %s, with tag %s", policyType, policyTag);
                }
                return String.format("Skipping unsupported PolicyType: %s", policyType);
            });
            return null;
        }

        final String id = policyDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = policyDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        final String name = EncodeDecodeUtils.encodePath(getSingleChildElementTextContent(policyDetails, NAME));

        final Element resources = getSingleChildElement(policy, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String policyString = resource.getTextContent();
        return new PolicyEntity.Builder()
                .setName(name)
                .setId(id)
                .setGuid(guid)
                .setParentFolderId(folderId)
                .setPolicyXML(policy)
                .setPolicy(policyString)
                .setTag(policyTag)
                .setPolicyType(PolicyType.fromType(policyType))
                .build();
    }

    @Override
    public Class<PolicyEntity> entityClass() {
        return PolicyEntity.class;
    }
}
