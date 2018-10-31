/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class PolicyLinker implements EntityLinker<PolicyEntity> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;

    @Inject
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
            Element policyElement = DocumentUtils.stringToXML(documentTools, policy.getPolicy());
            policyXMLSimplifier.simplifyPolicyXML(policyElement, bundle, targetBundle);
            policy.setPolicyXML(policyElement);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying policy: " + policy.getName() + " Message: " + e.getMessage(), e);
        }

        Folder folder = bundle.getFolderTree().getFolderById(policy.getFolderId());
        if (folder == null) {
            throw new LinkerException("Could not find folder for Policy. Policy Name:ID: " + policy.getName() + ":" + policy.getId() + ". Folder Id: " + policy.getFolderId());
        }
        Path folderPath = bundle.getFolderTree().getPath(folder);
        policy.setPolicyPath(Paths.get(folderPath.toString(), policy.getName() + ".xml").toString());
    }
}
