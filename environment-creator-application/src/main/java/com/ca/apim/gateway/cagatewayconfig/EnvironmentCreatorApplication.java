/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.google.common.annotations.VisibleForTesting;

import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.KeystoreCreator.createKeyStoreIfNecessary;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.APPLICATION;

public class EnvironmentCreatorApplication {

    @SuppressWarnings("squid:S1075") // this path is always fixed does not need to be customized.
    private static final String SYSTEM_PROPERTIES_PATH = "/opt/SecureSpan/Gateway/node/default/etc/conf/system.properties";

    private final Map<String, String> environmentProperties;
    private final String templatizedBundleFolderPath;
    private final String bootstrapBundleFolderPath;
    private final String keystoreFolderPath;
    private final String privateKeyFolderPath;

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
        String privateKeyFolderPath = args.length > 3 ? args[3] : "/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/privateKeys";

        new EnvironmentCreatorApplication(System.getenv(), templatizedBundleFolderPath, bootstrapBundleFolderPath, keystoreFolderPath, privateKeyFolderPath).run();
    }

    EnvironmentCreatorApplication(Map<String, String> environmentProperties,
                                  String templatizedBundleFolderPath,
                                  String bootstrapBundleFolderPath,
                                  String keystoreFolderPath,
                                  String privateKeyFolderPath) {
        this.environmentProperties = environmentProperties;
        this.templatizedBundleFolderPath = templatizedBundleFolderPath;
        this.bootstrapBundleFolderPath = bootstrapBundleFolderPath;
        this.keystoreFolderPath = keystoreFolderPath;
        this.privateKeyFolderPath = privateKeyFolderPath;
    }

    @VisibleForTesting
    void run() {
        //create bundle from environment
        EnvironmentBundleCreator bundleCreator = ConfigBuilderModule.getInjector().getInstance(EnvironmentBundleCreator.class);
        Bundle environmentBundle = bundleCreator.createEnvironmentBundle(
                environmentProperties,
                bootstrapBundleFolderPath,
                templatizedBundleFolderPath,
                APPLICATION,
                "_0_env.req.bundle"
        );

        // Create the KeyStore
        createKeyStoreIfNecessary(keystoreFolderPath, privateKeyFolderPath, environmentBundle.getPrivateKeys().values(), FileUtils.INSTANCE, SYSTEM_PROPERTIES_PATH);
    }


}
