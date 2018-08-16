/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.w3c.dom.Element;

public class PolicyLinker implements EntityLinker<PolicyEntity> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;

    PolicyLinker(DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.policyXMLSimplifier = PolicyXMLSimplifier.INSTANCE;
    }

    @Override
    public Class<PolicyEntity> getEntityClass() {
        return PolicyEntity.class;
    }

    @Override
    public void link(PolicyEntity policy, Bundle bundle, Bundle targetBundle) {
        try {
            Element policyElement = WriterHelper.stringToXML(documentTools, policy.getPolicy());
            policyXMLSimplifier.simplifyPolicyXML(policyElement, bundle, targetBundle);
            policy.setPolicyXML(policyElement);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying policy: " + policy.getName() + " Message: " + e.getMessage(), e);
        }
    }
}
