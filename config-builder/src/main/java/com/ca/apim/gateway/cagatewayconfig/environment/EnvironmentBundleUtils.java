/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;

/**
 * Utility methods for generating bundles with environment configurations.
 */
public class EnvironmentBundleUtils {

    private static final Logger logger = Logger.getLogger(EnvironmentBundleUtils.class.getName());

    private EnvironmentBundleUtils() {}

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
