/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PolicyLoader implements EntityLoader {
    private static final Logger LOGGER = Logger.getLogger(PolicyLoader.class.getName());

    @Override
    public Entity load(Element element) {
        final Element policy = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:Policy");
        final String guid = policy.getAttribute("guid");

        final Element policyDetails = EntityLoaderHelper.getSingleChildElement(policy, "l7:PolicyDetail");
        Element policyTypeElement = EntityLoaderHelper.getSingleChildElement(policyDetails, "l7:PolicyType");
        if (!("Include".equals(policyTypeElement.getTextContent()) || "Service Operation".equals(policyTypeElement.getTextContent()))) {
            LOGGER.log(Level.WARNING, "Skipping unsupported PolicyType: {0}", policyTypeElement.getTextContent());
            return null;
        }

        final String id = policyDetails.getAttribute("id");
        final String folderId = policyDetails.getAttribute("folderId");
        Element nameElement = EntityLoaderHelper.getSingleChildElement(policyDetails, "l7:Name");
        final String name = nameElement.getTextContent();

        final Element resources = EntityLoaderHelper.getSingleChildElement(policy, "l7:Resources");
        final Element resourceSet = EntityLoaderHelper.getSingleChildElement(resources, "l7:ResourceSet");
        final Element resource = EntityLoaderHelper.getSingleChildElement(resourceSet, "l7:Resource");
        final String policyString = resource.getTextContent();
        return new PolicyEntity(name, id, guid, folderId, policy, policyString);
    }
}