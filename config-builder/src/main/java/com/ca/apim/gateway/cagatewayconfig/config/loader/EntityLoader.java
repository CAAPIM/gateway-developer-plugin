/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;

import java.io.File;

/**
 * An Entity Loader is responsible for reading configuration files (either json/yaml or properties) and loading contents into their
 * java entity bean representation.
 */
public interface EntityLoader {

    /**
     * Load a single entity specified by name from its file. If the file contains more then one entity, only the one specified
     * is returned.
     *
     * @param name name of the entity to be loaded
     * @param entitiesFile file that contains the entity
     * @return the entity bean loaded
     */
    Object loadSingle(String name, File entitiesFile);

    /**
     * Load all entities from a configuration file into a Bundle object.
     *
     * @param bundle the bundle object to receive loaded entities
     * @param rootDir the directory containing the entity file (or subdir config)
     */
    void load(Bundle bundle, File rootDir);

    /**
     * Load a single entity into a bundle from a String representation of json/yaml or properties value.
     *
     * @param bundle the bundle to load the entity into
     * @param name name of the entity
     * @param value value to be loaded, either json or property value
     */
    void load(Bundle bundle, String name, String value);

    /**
     * @return the type of the entity managed by this loader
     */
    String getEntityType();
}
