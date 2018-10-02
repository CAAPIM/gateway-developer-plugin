/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvironmentCreatorApplication {
    private static final Logger logger = Logger.getLogger(EnvironmentCreatorApplication.class.getName());
    private final Map<String, String> environmentProperties;
    private final String templatizedBundleFolderPath;
    private final String bootstrapBundleFolderPath;
    private DocumentFileUtils documentFileUtils = DocumentFileUtils.INSTANCE;
    private DocumentTools documentTools = DocumentTools.INSTANCE;
    private IdGenerator idGenerator = new IdGenerator();

    /**
     * This application will build an environment bundle and detemplatize deployment bundles with environment configurations.
     *
     * @param args You can customize the folders that environment comes from by passing arguments.
     *             The first argument is the folder containing templatized bundles.
     *             The second parameter is the folder that bootstrap bundles should go into
     */
    public static void main(String[] args) {
        // consider using commons-cli if adding more parameters
        String templatizedBundleFolderPath = args.length > 0 ? args[0] : "/opt/docker/rc.d/bundle/templatized";
        String bootstrapBundleFolderPath = args.length > 1 ? args[1] : "/opt/SecureSpan/Gateway/node/default/etc/bootstrap/bundle/";

        new EnvironmentCreatorApplication(System.getenv(), templatizedBundleFolderPath, bootstrapBundleFolderPath).run();
    }

    EnvironmentCreatorApplication(Map<String, String> environmentProperties, String templatizedBundleFolderPath, String bootstrapBundleFolderPath) {
        this.environmentProperties = environmentProperties;
        this.templatizedBundleFolderPath = templatizedBundleFolderPath;
        this.bootstrapBundleFolderPath = bootstrapBundleFolderPath;
    }

    @VisibleForTesting
    void run() {
        //create bundle from environment
        EnvironmentBundleBuilder environmentBundleBuilder = new EnvironmentBundleBuilder(environmentProperties);

        // detempatize bundle deployment bundles
        File templatizedFolder = new File(templatizedBundleFolderPath);
        File[] templatizedBundles = templatizedFolder.listFiles((dir, name) -> name.endsWith(".bundle"));
        if (templatizedBundles != null) {
            Arrays.asList(templatizedBundles)
                    .forEach(templatizedBundle ->
                            detemplatizeBundleFile(environmentBundleBuilder.getBundle().getEnvironmentProperties(), templatizedBundle, new File(bootstrapBundleFolderPath, templatizedBundle.getName())));
        }

        //TODO: check deployment bundles to validated that all required environment is provided.

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        BundleEntityBuilder bundleEntityBuilder = new BundleEntityBuilder(documentFileUtils, documentTools, document, idGenerator);

        Element bundleElement = bundleEntityBuilder.build(environmentBundleBuilder.getBundle(), EntityBuilder.BundleType.ENVIRONMENT);
        documentFileUtils.createFile(bundleElement, new File(bootstrapBundleFolderPath, "_env.req.bundle").toPath());
    }

    private static void detemplatizeBundleFile(Map<String, String> properties, File bundleFile, File targetBundleFile) {
        logger.log(Level.FINE, () -> "Detemplatizing bundle: " + bundleFile.getAbsolutePath());
        String bundleString;
        try {
            bundleString = new String(Files.readAllBytes(bundleFile.toPath()));
        } catch (IOException e) {
            throw new BundleDetemplatizeException("Could not read bundle file for detemplatization: " + bundleFile, e);
        }

        CharSequence detemplatizedBundle = new BundleDetemplatizer(properties).detemplatizeBundleString(bundleString);

        try {
            Files.write(targetBundleFile.toPath(), detemplatizedBundle.toString().getBytes());
        } catch (IOException e) {
            throw new BundleDetemplatizeException("Could not write detemplatized bundle to: " + targetBundleFile, e);
        }
    }
}
