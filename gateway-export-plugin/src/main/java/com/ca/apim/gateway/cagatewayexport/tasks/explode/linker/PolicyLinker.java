/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Singleton
public class PolicyLinker implements EntityLinker<Policy> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;

    @Inject
    PolicyLinker(DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.policyXMLSimplifier = PolicyXMLSimplifier.INSTANCE;
    }

    @Override
    public Class<Policy> getEntityClass() {
        return Policy.class;
    }

    @Override
    public void link(Bundle filteredBundle, Bundle bundle) {
        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(p -> link(p, bundle, filteredBundle));
    }

    @Override
    public void link(Policy policy, Bundle bundle, Bundle targetBundle) {
        try {
            Element policyElement = DocumentUtils.stringToXML(documentTools, policy.getPolicyXML());
            policyXMLSimplifier.simplifyPolicyXML(policyElement, bundle, targetBundle);
            policy.setPolicyDocument(policyElement);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying policy: " + policy.getName() + " Message: " + e.getMessage(), e);
        }

        policy.setPath(getPolicyPath(policy, bundle, policy));
    }

    static <E extends GatewayEntity> String getPolicyPath(Policy policy, Bundle bundle, E entity) {
        Folder folder = bundle.getFolderTree().getFolderById(policy.getParentFolder().getId());
        if (folder == null) {
            throw new LinkerException(String.format("Could not find folder for %s: %s. Policy Name:ID: %s:%s. Folder ID: %s",
                    entity.getClass().getAnnotation(Named.class).value(),
                    entity.getName(),
                    policy.getName(),
                    policy.getId(),
                    policy.getParentFolder().getId()));
        }
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return PathUtils.unixPath(folderPath.toString(), policy.getName());
    }
}
