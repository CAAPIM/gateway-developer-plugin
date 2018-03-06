/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder;

import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ServiceEntityBuilder implements EntityBuilder<Service> {
    private final Document document;

    public ServiceEntityBuilder(Document document) {
        this.document = document;
    }

    @Override
    public Service build(String name, String id, Element entityElement, File folder, String parentFolderID) {
        entityElement.setAttribute("id", id);
        entityElement.setAttribute("folderId", parentFolderID);
        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(name);
        entityElement.insertBefore(nameElement, entityElement.getFirstChild());

        Element serviceElement = document.createElement("l7:Service");
        serviceElement.setAttribute("id", id);
        serviceElement.appendChild(entityElement);

        Element resourcesElement = document.createElement("l7:Resources");

        Element resourceSetElement = document.createElement("l7:ResourceSet");
        resourceSetElement.setAttribute("tag", "policy");

        Element resourceElement = document.createElement("l7:Resource");
        resourceElement.setAttribute("type", "policy");

        File policyFile = new File(folder, name + ".policy.xml");
        String policyString;
        try {
            policyString = new String(Files.readAllBytes(policyFile.toPath()));
        } catch (IOException e) {
            throw new EntityBuilderException("Could not load service policy from file: " + policyFile, e);
        }
        resourceElement.setTextContent(policyString);

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        serviceElement.appendChild(resourcesElement);

        return new Service(name, id, parentFolderID, serviceElement, entityElement, policyString);
    }
}
