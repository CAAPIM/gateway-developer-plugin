/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty.Type.SERVICE;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ServiceLinker implements EntityLinker<ServiceEntity> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;

    @Inject
    ServiceLinker(DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.policyXMLSimplifier = PolicyXMLSimplifier.INSTANCE;
    }

    @Override
    public Class<ServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    public void link(ServiceEntity service, Bundle bundle, Bundle targetBundle) {
        try {
            Element policyElement = DocumentUtils.stringToXML(documentTools, service.getPolicy());
            policyXMLSimplifier.simplifyPolicyXML(policyElement, bundle, targetBundle);
            service.setPolicyXML(policyElement);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying service: " + service.getName() + " Message: " + e.getMessage(), e);
        }
        service.setPath(getServicePath(bundle, service));

        Element servicePropertiesElement = getSingleChildElement(service.getServiceDetailsElement(), PROPERTIES);
        NodeList propertyNodes = servicePropertiesElement.getElementsByTagName(PROPERTY);
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            if (propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.ENV.")) {
                targetBundle.addEntity(new EnvironmentProperty(
                        propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().substring(13),
                        getSingleChildElement((Element) propertyNodes.item(i), STRING_VALUE).getTextContent(), SERVICE));
            }
        }
    }

    /**
     * Find the full path for a service entity.
     *
     * @param bundle the bundle
     * @param serviceEntity the service entity
     * @return the full path for the specified service
     */
    static String getServicePath(Bundle bundle, ServiceEntity serviceEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(serviceEntity.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), serviceEntity.getName()).toString();
    }
}
