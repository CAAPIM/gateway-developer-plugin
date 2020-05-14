/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.MissingGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * Simplifier for the include assertion.
 */
@Singleton
public class IncludeAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(IncludeAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element assertionElement = context.getAssertionElement();
        Bundle bundle = context.getBundle();
        Bundle resultantBundle = context.getResultantBundle();

        Element policyGuidElement = getSingleElement(assertionElement, POLICY_GUID);
        String includedPolicyGuid = policyGuidElement.getAttribute(STRING_VALUE);
        Optional<Policy> resultantPolicyEntity = resultantBundle.getEntities(Policy.class).values().stream().filter(p -> includedPolicyGuid.equals(p.getGuid())).findAny();
        if (resultantPolicyEntity.isPresent()) {
            policyGuidElement.setAttribute("policyPath", getPolicyPath(bundle, resultantPolicyEntity.get()));
            policyGuidElement.removeAttribute(STRING_VALUE);
        } else {
            boolean excluded = true;
            Optional<Policy> policyEntity = bundle.getEntities(Policy.class).values().stream().filter(p -> includedPolicyGuid.equals(p.getGuid())).findAny();
            if (!policyEntity.isPresent()) {
                LOGGER.log(Level.WARNING, "Could not find referenced policy include with guid: {0}", includedPolicyGuid);
                excluded = false;
            }

            final MissingGatewayEntity missingEntity = new MissingGatewayEntity();
            missingEntity.setType("POLICY");
            missingEntity.setName("Policy#" + includedPolicyGuid);
            missingEntity.setGuid(includedPolicyGuid);
            missingEntity.setExcluded(excluded);
            missingEntity.setId(missingEntity.getGuid().replace("-", ""));

            context.getResultantBundle().addEntity(missingEntity);

            LOGGER.log(Level.WARNING, "Recording the referenced policy include with guid: {0} as {1} entity",
                    new Object[] {includedPolicyGuid, excluded ? "excluded" : "missing"});

            policyGuidElement.setAttribute("policyPath", missingEntity.getName());
            policyGuidElement.removeAttribute(STRING_VALUE);
        }
    }

    private String getPolicyPath(Bundle bundle, Policy policyEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(policyEntity.getParentFolder().getId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return PathUtils.unixPath(folderPath.toString(), policyEntity.getName());
    }

    @Override
    public String getAssertionTagName() {
        return INCLUDE;
    }
}
