package com.ca.apim.gateway.cagatewayexport.tasks.explode;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.w3c.dom.Document;

import java.io.File;
import java.util.Collection;

public class ExplodeBundle {
    private final DocumentTools documentTools;
    private final EntityWriterRegistry entityWriterRegistry;

    public ExplodeBundle(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentTools = documentTools;
        this.entityWriterRegistry = new EntityWriterRegistry(documentTools, documentFileUtils, jsonTools);

    }

    public void explodeBundle(File bundleFile, File explodeDirectory) throws DocumentParseException {
        final Document bundleDocument = documentTools.parse(bundleFile);
        documentTools.cleanup(bundleDocument);

        final BundleBuilder bundleBuilder = new BundleBuilder();
        bundleBuilder.buildBundle(bundleDocument.getDocumentElement());

        //build folder structure
        Bundle bundle = bundleBuilder.getBundle();

        final Collection<EntityWriter> entityBuilders = entityWriterRegistry.getEntityWriters();
        entityBuilders.parallelStream().forEach(e -> e.write(bundle, explodeDirectory));
    }

}
