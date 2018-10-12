/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.KeystoreCreator.createKeyStoreIfNecessary;

public class EnvironmentCreatorApplication {

    private static final Logger logger = Logger.getLogger(EnvironmentCreatorApplication.class.getName());

    private final Map<String, String> environmentProperties;
    private final String templatizedBundleFolderPath;
    private final String bootstrapBundleFolderPath;
    private final String keystoreFolderPath;
    private DocumentFileUtils documentFileUtils = DocumentFileUtils.INSTANCE;
    private DocumentTools documentTools = DocumentTools.INSTANCE;

    /**
     * This application will build an environment bundle and detemplatize deployment bundles with environment configurations.
     *
     * @param args You can customize the folders that environment comes from by passing arguments.
     *             The first argument is the folder containing templatized bundles.
     *             The second parameter is the folder that bootstrap bundles should go into
     *             The third parameter is the folder that the keystore file if necessary should be placed
     */
    public static void main(String[] args) {
        // consider using commons-cli if adding more parameters
        String templatizedBundleFolderPath = args.length > 0 ? args[0] : "/opt/docker/rc.d/bundle/templatized";
        String bootstrapBundleFolderPath = args.length > 1 ? args[1] : "/opt/SecureSpan/Gateway/node/default/etc/bootstrap/bundle/";
        String keystoreFolderPath = args.length > 2 ? args[2] : "/opt/docker/rc.d/keystore";

        new EnvironmentCreatorApplication(System.getenv(), templatizedBundleFolderPath, bootstrapBundleFolderPath, keystoreFolderPath).run();
    }

    EnvironmentCreatorApplication(Map<String, String> environmentProperties,
                                  String templatizedBundleFolderPath,
                                  String bootstrapBundleFolderPath,
                                  String keystoreFolderPath) {
        this.environmentProperties = environmentProperties;
        this.templatizedBundleFolderPath = templatizedBundleFolderPath;
        this.bootstrapBundleFolderPath = bootstrapBundleFolderPath;
    }

    @VisibleForTesting
    void run() {
        //create bundle from environment
        EntityLoaderRegistry entityLoaderRegistry = ConfigBuilderModule.getInjector().getInstance(EntityLoaderRegistry.class);
        EnvironmentBundleBuilder environmentBundleBuilder = new EnvironmentBundleBuilder(environmentProperties, entityLoaderRegistry);
        Bundle environmentBundle = environmentBundleBuilder.getBundle();

        processDeploymentBundles(environmentBundle);

        // write the Environment bundle
        final DocumentBuilder documentBuilder = documentTools.getDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        BundleEntityBuilder bundleEntityBuilder = ConfigBuilderModule.getInjector().getInstance(BundleEntityBuilder.class);

        Element bundleElement = bundleEntityBuilder.build(environmentBundle, EntityBuilder.BundleType.ENVIRONMENT, document);
        documentFileUtils.createFile(bundleElement, new File(bootstrapBundleFolderPath, "_0_env.req.bundle").toPath());

        // Create the KeyStore
        createKeyStoreIfNecessary(environmentProperties, keystoreFolderPath);
    }

    private void processDeploymentBundles(Bundle environmentBundle) {
        File templatizedFolder = new File(templatizedBundleFolderPath);
        File[] templatizedBundles = templatizedFolder.listFiles((dir, name) -> name.endsWith(".bundle"));
        if (templatizedBundles != null) {
            BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
            BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(environmentBundle.getEnvironmentProperties());
            Arrays.asList(templatizedBundles)
                    .forEach(templatizedBundle -> {
                        logger.log(Level.FINE, () -> "Processing deployment bundle: " + templatizedBundle);
                        String bundleString;
                        try {
                            bundleString = new String(Files.readAllBytes(templatizedBundle.toPath()));
                        } catch (IOException e) {
                            throw new BundleDetemplatizeException("Could not read bundle file: " + templatizedBundle, e);
                        }

                        // detempatize deployment bundles
                        CharSequence detemplatizedBundle = bundleDetemplatizer.detemplatizeBundleString(bundleString);
                        Path bootstrapBundleFilePath = new File(bootstrapBundleFolderPath, templatizedBundle.getName()).toPath();
                        try {
                            Files.write(bootstrapBundleFilePath, detemplatizedBundle.toString().getBytes());
                        } catch (IOException e) {
                            throw new BundleDetemplatizeException("Could not write detemplatized bundle to: " + bootstrapBundleFilePath, e);
                        }
                        // check deployment bundles to validated that all required environment is provided.
                        bundleEnvironmentValidator.validateEnvironmentProvided(templatizedBundle.getName(), bundleString);
                    });
        }
    }
}
