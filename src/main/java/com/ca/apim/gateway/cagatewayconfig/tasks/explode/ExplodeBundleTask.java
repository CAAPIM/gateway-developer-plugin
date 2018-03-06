/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode;

import com.ca.apim.gateway.cagatewayconfig.bundle.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.BundleBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.BundleBuilderException;
import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.FolderWriter;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.ServiceWriter;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;

import javax.inject.Inject;
import java.util.Map;

public class ExplodeBundleTask extends DefaultTask {
    private final DocumentFileUtils documentFileUtils;
    private DocumentTools documentTools;

    private RegularFileProperty inputBundleFile;
    private DirectoryProperty exportDir;

    @Inject
    public ExplodeBundleTask() {
        this(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE);
    }

    private ExplodeBundleTask(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils) {
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
        inputBundleFile = newInputFile();
        exportDir = newOutputDirectory();
    }

    @InputFile
    public RegularFileProperty getInputBundleFile() {
        return inputBundleFile;
    }

    @OutputDirectory
    public DirectoryProperty getExportDir() {
        return exportDir;
    }

    @TaskAction
    public void perform() throws DocumentParseException {
        final Document bundleDocument = documentTools.parse(inputBundleFile.getAsFile().get());
        documentTools.cleanup(bundleDocument);

        final BundleBuilder bundleBuilder = new BundleBuilder();
        bundleBuilder.buildBundle(bundleDocument.getDocumentElement());

        //build folder structure
        Bundle bundle = bundleBuilder.getBundle();

        FolderTree folderTree = bundle.getFolderTree();
        EntityWriter<Folder> folderWriter = getEntityWriter(folderTree, Folder.class);
        folderTree.stream().forEach(f -> folderWriter.write(exportDir.getAsFile().get().toPath(), f));

        Map<Class<? extends Entity>, Map<String, Entity>> entities = bundle.getAllEntities();
        entities.forEach((entityType, entitiesMap) -> {
            if (entityType != Folder.class) {
                entitiesMap.values().forEach(e -> getEntityWriter(folderTree, entityType).write(exportDir.getAsFile().get().toPath(), e));
            }
        });
    }

    private EntityWriter getEntityWriter(FolderTree folderTree, Class<? extends Entity> entityType) {
        if (Folder.class == entityType) {
            return new FolderWriter(folderTree);
        } else if (Service.class == entityType) {
            return new ServiceWriter(folderTree, documentTools, documentFileUtils);
        } else
            throw new BundleBuilderException("No entity loader found for entity type: " + entityType);
    }
}
