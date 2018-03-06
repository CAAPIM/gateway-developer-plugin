/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.bundle.BundleBuilderException;
import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleDocumentBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder.FolderEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder.ServiceEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;

public class ZipBundleTask extends DefaultTask {

    private final Document document;
    private final DocumentBuilder documentBuilder;
    private final DocumentFileUtils documentFileUtils;
    private final IdGenerator idGenerator;
    private DirectoryProperty inputDir;
    private RegularFileProperty outputBundleFile;

    @Inject
    public ZipBundleTask() {
        this(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE);
    }

    public ZipBundleTask(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils) {
        outputBundleFile = newOutputFile();
        inputDir = newInputDirectory();

        this.idGenerator = new IdGenerator();
        this.documentFileUtils = documentFileUtils;

        documentBuilder = documentTools.getDocumentBuilder();
        document = documentBuilder.newDocument();
    }

    @InputDirectory
    public DirectoryProperty getInputDir() {
        return inputDir;
    }

    @OutputFile
    public RegularFileProperty getOutputBundleFile() {
        return outputBundleFile;
    }

    @TaskAction
    public void perform() throws IOException, SAXException {
        final File rootDir = inputDir.getAsFile().get();

        FolderEntityBuilder folderEntityBuilder = (FolderEntityBuilder) getEntityElementBuilder(Folder.class);
        Folder rootFolder = folderEntityBuilder.buildRootFolder();

        BundleDocumentBuilder bundleDocumentBuilder = new BundleDocumentBuilder(document).withFolder(rootFolder);
        collectFolderableEntities(rootFolder, rootDir, bundleDocumentBuilder);

        Element bundle = bundleDocumentBuilder.build();
        documentFileUtils.createFile(bundle, outputBundleFile.getAsFile().get().toPath(), true);
    }

    private void collectFolderableEntities(Folder parentFolder, File dir, BundleDocumentBuilder bundleDocumentBuilder) throws SAXException, IOException {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    Folder folder = loadChildFolder(child, parentFolder.getId());
                    bundleDocumentBuilder.withFolder(folder);
                    collectFolderableEntities(folder, child, bundleDocumentBuilder);
                } else {
                    if (!child.getName().endsWith(".policy.xml") && !child.getName().equals("folder.xml")) {
                        bundleDocumentBuilder.withEntity(loadEntity(child, parentFolder.getId()));
                    }
                }
            }
        }
    }

    private Entity loadEntity(File child, String parentFolderID) throws IOException, SAXException {
        Document doc = documentBuilder.parse(child);
        Element documentElement = (Element) document.importNode(doc.getDocumentElement(), true);

        final EntityBuilder entityBuilder = getEntityBuilderFromTagName(documentElement.getTagName());
        return entityBuilder.build(child.getName().substring(0, child.getName().lastIndexOf('.')), idGenerator.generate(), documentElement, child.getParentFile(), parentFolderID);
    }

    private EntityBuilder getEntityBuilderFromTagName(String tagName) {
        switch (tagName) {
            case "l7:ServiceDetail":
                return getEntityElementBuilder(Service.class);
            default:
                throw new BundleBuilderException("Unknown Tag: " + tagName);
        }
    }

    private Folder loadChildFolder(File child, String parentFolderID) {
        FolderEntityBuilder folderEntityBuilder = (FolderEntityBuilder) getEntityElementBuilder(Folder.class);
        return folderEntityBuilder.buildFolder(child.getName(), idGenerator.generate(), parentFolderID);
    }

    private EntityBuilder getEntityElementBuilder(Class<? extends Entity> entityType) {
        if (Folder.class == entityType) {
            return new FolderEntityBuilder(document);
        } else if (Service.class == entityType) {
            return new ServiceEntityBuilder(document);
        } else
            throw new BundleBuilderException("No entity loader found for entity type: " + entityType);
    }
}
