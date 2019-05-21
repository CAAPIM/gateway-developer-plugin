/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingMode;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

/**
 * Utility methods for generating bundles with environment configurations.
 */
public class EnvironmentBundleUtils {

    private static final Logger logger = Logger.getLogger(EnvironmentBundleUtils.class.getName());

    private static String templatizedBundlesFolderPath;

    private EnvironmentBundleUtils() {}

    static void setTemplatizedBundlesFolderPath(String folderPath) {
        templatizedBundlesFolderPath = folderPath;
    }

    public static Bundle getDeploymentBundle() {
        if (templatizedBundlesFolderPath == null) {
            throw new BundleLoadException("Invalid deployment bundle path : " + templatizedBundlesFolderPath);
        }

        BundleCache cache = InjectionRegistry.getInjector().getInstance(BundleCache.class);

        if (cache.contains(templatizedBundlesFolderPath)) {
            return cache.getBundle(templatizedBundlesFolderPath);
        } else {
            EntityBundleLoader loader = InjectionRegistry.getInjector().getInstance(EntityBundleLoader.class);
            List<File> deploymentBundleFiles = collectFiles(templatizedBundlesFolderPath, BUNDLE_EXTENSION);
            cache.putBundle(templatizedBundlesFolderPath, loader.load(deploymentBundleFiles, BundleLoadingMode.STRICT));
            return loader.load(deploymentBundleFiles, BundleLoadingMode.STRICT);
        }
    }

    static void processDeploymentBundles(Bundle environmentBundle,
                                          List<TemplatizedBundle> templatizedBundles,
                                          EnvironmentBundleCreationMode mode) {
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(environmentBundle);
        templatizedBundles.forEach(tb -> processTemplatizedBundle(tb, bundleEnvironmentValidator, bundleDetemplatizer, mode));
    }

    private static void processTemplatizedBundle(TemplatizedBundle templatizedBundle,
                                                 BundleEnvironmentValidator bundleEnvironmentValidator,
                                                 BundleDetemplatizer bundleDetemplatizer,
                                                 EnvironmentBundleCreationMode mode) {
        logger.log(Level.FINE, () -> "Processing deployment bundle: " + templatizedBundle.getName());
        String bundleString = templatizedBundle.getContents();

        // check deployment bundles to validated that all required environment is provided.
        bundleEnvironmentValidator.validateEnvironmentProvided(templatizedBundle.getName(), bundleString, mode);

        // detempatize bundle
        CharSequence detemplatizedBundle = bundleDetemplatizer.detemplatizeBundleString(bundleString);
        templatizedBundle.writeContents(detemplatizedBundle.toString());
    }

    @VisibleForTesting
    public static String buildBundleItemKey(Element item) {
        return getSingleChildElementTextContent(item, ID) + ":" + getSingleChildElementTextContent(item, TYPE);
    }

    @VisibleForTesting
    public static String buildBundleMappingKey(Element mapping) {
        return mapping.getAttribute(ATTRIBUTE_SRCID) + ":" + mapping.getAttribute(ATTRIBUTE_TYPE);
    }
}
