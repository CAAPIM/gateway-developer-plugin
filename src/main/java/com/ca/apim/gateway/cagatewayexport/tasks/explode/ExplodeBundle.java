/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.BundleFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.injection.ExportPluginModule;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
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

    void explodeBundle(String folderPath, File bundleFile, File explodeDirectory) throws DocumentParseException {
        final Document bundleDocument = documentTools.parse(bundleFile);
        documentTools.cleanup(bundleDocument);

        //loads the bundle
        final BundleBuilder bundleBuilder = ExportPluginModule.getInjector().getInstance(BundleBuilder.class);
        Bundle bundle = bundleBuilder.buildBundle(bundleDocument.getDocumentElement());

        //filter out unwanted entities
        BundleFilter bundleFilter = new BundleFilter(bundle);
        Bundle filteredBundle = bundleFilter.filter(folderPath);

        //Link, simplify and process entities
        final Collection<EntitiesLinker> entityLinkers = entityLinkerRegistry.getEntityLinkers();
        entityLinkers.parallelStream().forEach(e -> e.link(filteredBundle, bundle));

        //write the bundle in the exploded format
        final Collection<EntityWriter> entityBuilders = entityWriterRegistry.getEntityWriters();
        entityBuilders.parallelStream().forEach(e -> e.write(filteredBundle, explodeDirectory));
    }

}
