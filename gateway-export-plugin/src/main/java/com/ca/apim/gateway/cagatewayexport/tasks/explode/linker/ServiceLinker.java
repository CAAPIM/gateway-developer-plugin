/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.policy.ServicePolicyXMLSimplifier;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.L7_TEMPLATE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class ServiceLinker implements EntityLinker<Service> {
    private final DocumentTools documentTools;
    private final PolicyXMLSimplifier policyXMLSimplifier;
    private final ServicePolicyXMLSimplifier servicePolicyXMLSimplifier;

    @Inject
    ServiceLinker(DocumentTools documentTools, PolicyXMLSimplifier policyXMLSimplifier, ServicePolicyXMLSimplifier servicePolicyXMLSimplifier) {
        this.documentTools = documentTools;
        this.policyXMLSimplifier = policyXMLSimplifier;
        this.servicePolicyXMLSimplifier = servicePolicyXMLSimplifier;
    }

    @Override
    public Class<Service> getEntityClass() {
        return Service.class;
    }

    @Override
    public void link(Service service, Bundle bundle, Bundle targetBundle) {
        String portalManagedService;
        try {
            Element policyElement = DocumentUtils.stringToXML(documentTools, service.getPolicy());
            policyXMLSimplifier.simplifyPolicyXML(policyElement, service.getName(), bundle, targetBundle);
            servicePolicyXMLSimplifier.simplifyServicePolicyXML(policyElement, service);
            service.setPolicyXML(policyElement);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception linking and simplifying service: " + service.getName() + " Message: " + e.getMessage(), e);
        }
        service.setPath(getServicePath(bundle, service));

        Element servicePropertiesElement = getSingleChildElement(service.getServiceDetailsElement(), PROPERTIES);
        NodeList propertyNodes = servicePropertiesElement.getElementsByTagName(PROPERTY);

        for (int i = 0; i < propertyNodes.getLength(); i++) {
            if (propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().startsWith("property.ENV.")) {
                ServiceEnvironmentProperty serviceEnvironmentProperty = new ServiceEnvironmentProperty(
                        service.getName() + "." + propertyNodes.item(i).getAttributes().getNamedItem("key").getTextContent().substring(13),
                        getSingleChildElement((Element) propertyNodes.item(i), STRING_VALUE).getTextContent());
                targetBundle.getEntities(ServiceEnvironmentProperty.class).put(serviceEnvironmentProperty.getName(), serviceEnvironmentProperty);
            }
        }

        if ("true".equals(service.getProperties().get(L7_TEMPLATE)) &&
                ServiceAndPolicyLoaderUtil.migratePortalIntegrationsAssertions() && !service.isBundle()) {
            service.setAnnotations(new HashSet<>(Collections.singletonList(new Annotation(AnnotationType.BUNDLE))));
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
