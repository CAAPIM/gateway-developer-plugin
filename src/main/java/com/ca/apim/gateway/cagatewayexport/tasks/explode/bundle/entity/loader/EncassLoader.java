/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import org.w3c.dom.Element;

public class EncassLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element encass = EntityLoaderHelper.getSingleElement(element, "l7:EncapsulatedAssertion");

        final Element policyReference = EntityLoaderHelper.getSingleElement(encass, "l7:PolicyReference");
        final String policyId = policyReference.getAttribute("id");
        Element nameElement = EntityLoaderHelper.getSingleElement(encass, "l7:Name");
        final String name = nameElement.getTextContent();
        Element guidElement = EntityLoaderHelper.getSingleElement(encass, "l7:Guid");
        final String guid = guidElement.getTextContent();
        return new EncassEntity(name, encass.getAttribute("id"), guid, encass, policyId);
    }
}
