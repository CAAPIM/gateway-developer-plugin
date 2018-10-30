/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.w3c.dom.Element;

public class TestUtils {

    public static PolicyEntity createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyXML, String policy) {
        return new PolicyEntity.Builder().setName(name).setId(id).setGuid(guid).setParentFolderId(parentFolderId).setPolicyXML(policyXML).setPolicy(policy).build();
    }
}
