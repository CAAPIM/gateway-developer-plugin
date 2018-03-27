/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public class BundleBuilder {

    private final DocumentFileUtils documentFileUtils;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final BundleEntityBuilder bundleEntityBuilder;

    public BundleBuilder(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, final FileUtils fileUtils, final JsonTools jsonTools) {
        IdGenerator idGenerator = new IdGenerator();
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        this.documentFileUtils = documentFileUtils;
        this.entityLoaderRegistry = new EntityLoaderRegistry(fileUtils, jsonTools);
        this.bundleEntityBuilder = new BundleEntityBuilder(documentFileUtils, documentTools, document, idGenerator);
    }

    public void buildBundle(File rootDir, Path outputPath) {

        final Collection<EntityLoader> entityBuilders = entityLoaderRegistry.getEntityLoaders();
        final Bundle bundle = new Bundle();

        //Load
        entityBuilders.parallelStream().forEach(e -> e.load(bundle, rootDir));

        //Zip
        Element bundleElement = bundleEntityBuilder.build(bundle);
        documentFileUtils.createFile(bundleElement, outputPath);

    }


}
