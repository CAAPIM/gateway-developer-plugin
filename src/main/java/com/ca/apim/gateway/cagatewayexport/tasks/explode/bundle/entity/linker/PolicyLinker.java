/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;

public class PolicyLinker implements EntityLinker<PolicyEntity> {
    private final DocumentTools documentTools;

    public PolicyLinker(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    public void link(Bundle filteredBundle, Bundle bundle) {
        filteredBundle.getEntities(PolicyEntity.class).values().forEach(p -> link(p, bundle));
    }

    private void link(PolicyEntity policy, Bundle bundle) {
        try {
            policy.setXML(PolicyXMLSimplifier.simplifyPolicyXML(WriterHelper.stringToXML(documentTools, policy.getPolicy()), bundle));
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying policy: " + policy.getName() + " Message: " + e.getMessage(), e);
        }
    }
}
