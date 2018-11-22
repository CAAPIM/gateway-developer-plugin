/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty.Type;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ServiceLinker implements EntityLinker<Service> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;

    @Inject
    ServiceLinker(DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.policyXMLSimplifier = PolicyXMLSimplifier.INSTANCE;
    }

    @Override
    public Class<Service> getEntityClass() {
        return Service.class;
    }

    @Override
    public void link(Service service, Bundle bundle, Bundle targetBundle) {
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
                EnvironmentProperty environmentProperty = new EnvironmentProperty(
                        propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().substring(13),
                        getSingleChildElement((Element) propertyNodes.item(i), STRING_VALUE).getTextContent(), Type.SERVICE);
                targetBundle.getEntities(EnvironmentProperty.class).put(environmentProperty.getId(), environmentProperty);
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
    static String getServicePath(Bundle bundle, Service serviceEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(serviceEntity.getParentFolder().getId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return PathUtils.unixPath(folderPath.toString(), serviceEntity.getName());
    }
}
