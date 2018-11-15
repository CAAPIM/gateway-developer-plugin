/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.BundleFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import org.w3c.dom.Document;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;

public class ExplodeBundle {
    private final DocumentTools documentTools;
    private final EntityWriterRegistry entityWriterRegistry;
    private final EntityLinkerRegistry entityLinkerRegistry;

    @Inject
    ExplodeBundle(final DocumentTools documentTools,
                  final EntityWriterRegistry entityWriterRegistry,
                  final EntityLinkerRegistry entityLinkerRegistry) {
        this.documentTools = documentTools;
        this.entityWriterRegistry = entityWriterRegistry;
        this.entityLinkerRegistry = entityLinkerRegistry;
    }

    void explodeBundle(String folderPath, FilterConfiguration filterConfiguration, File bundleFile, File explodeDirectory) throws DocumentParseException {
        final Document bundleDocument = documentTools.parse(bundleFile);
        documentTools.cleanup(bundleDocument);

        //loads the bundle
        final BundleBuilder bundleBuilder = ExportPluginModule.getInjector().getInstance(BundleBuilder.class);
        Bundle bundle = bundleBuilder.buildBundle(bundleDocument.getDocumentElement());

        //filter out unwanted entities
        BundleFilter bundleFilter = ExportPluginModule.getInjector().getInstance(BundleFilter.class);
        Bundle filteredBundle = bundleFilter.filter(folderPath, filterConfiguration, bundle);
        
        bundleFilter.verifyImportedFolder(folderPath, filteredBundle);

        //Link, simplify and process entities
        final Collection<EntitiesLinker> entityLinkers = entityLinkerRegistry.getEntityLinkers();
        entityLinkers.parallelStream().forEach(e -> e.link(filteredBundle, bundle, explodeDirectory));

        //write the bundle in the exploded format
        final Collection<EntityWriter> entityBuilders = entityWriterRegistry.getEntityWriters();
        entityBuilders.parallelStream().forEach(e -> e.write(filteredBundle, explodeDirectory));
    }

}
