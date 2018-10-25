/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class PolicyLoader implements EntityLoader<PolicyEntity> {
    private static final Logger LOGGER = Logger.getLogger(PolicyLoader.class.getName());

    @Override
    public PolicyEntity load(Element element) {
        final Element policy = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY);
        final String guid = policy.getAttribute(ATTRIBUTE_GUID);

        final Element policyDetails = getSingleChildElement(policy, POLICY_DETAIL);
        Element policyTypeElement = getSingleChildElement(policyDetails, POLICY_TYPE);
        if (!("Include".equals(policyTypeElement.getTextContent()) || "Service Operation".equals(policyTypeElement.getTextContent()))) {
            LOGGER.log(Level.WARNING, "Skipping unsupported PolicyType: {0}", policyTypeElement.getTextContent());
            return null;
        }

        final String id = policyDetails.getAttribute(ATTRIBUTE_ID);
        final String folderId = policyDetails.getAttribute(ATTRIBUTE_FOLDER_ID);
        Element nameElement = getSingleChildElement(policyDetails, NAME);
        final String name = nameElement.getTextContent();

        final Element resources = getSingleChildElement(policy, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String policyString = resource.getTextContent();
        return new PolicyEntity(name, id, guid, folderId, policy, policyString);
    }

    @Override
    public Class<PolicyEntity> entityClass() {
        return PolicyEntity.class;
    }
}