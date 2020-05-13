/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.util.List;

/**
 * An implementation of entity builder is responsible for collecting the entity information stored in yaml/json/properties files or environment properties
 * and convert this data to a Gateway restman bundle format.
 * <p>
 * ex: (input) some-entity-file.json -- SomeEntityBuilder -- Bundle XML element (output)
 */
public interface EntityBuilder extends Comparable<EntityBuilder> {

    List<Entity> build(Bundle bundle, BundleType bundleType, Document document);

    /**
     * Types of bundles.
     */
    enum BundleType {
        /**
         * this is the bundle created with the entities that require changes related on where the GW is deployed
         */
        ENVIRONMENT,
        /**
         * this is the bundle that contains gateway behaviour like policies/services/encasses and its dependencies to environment variables
         */
        DEPLOYMENT
    }

    /**
     * @return the position of the entity produced by this builder in the bundle file, has to be non null and not conflicting
     */
    @NotNull
    Integer getOrder();

    @Override
    default int compareTo(@NotNull EntityBuilder o) {
        return this.getOrder().compareTo(o.getOrder());
    }
}
