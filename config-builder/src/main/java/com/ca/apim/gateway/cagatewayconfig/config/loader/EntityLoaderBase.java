/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.loadEntitiesFromFile;
import static com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils.tryInferContentTypeFromValue;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Base loader for all entities with information stored in yaml or json files.
 *
 * @param <B> type of entity
 */
public abstract class EntityLoaderBase<B extends GatewayEntity> implements EntityLoader {

    private final JsonTools jsonTools;
    private IdGenerator idGenerator;

    EntityLoaderBase(final JsonTools jsonTools, final IdGenerator idGenerator) {
        this.jsonTools = jsonTools;
        this.idGenerator = idGenerator;
    }

    public Object loadSingle(String name, File entitiesFile) {
        return loadEntitiesFromFile(jsonTools, this.getBeanClass(), entitiesFile).get(name);
    }

    public Map<String, Object> load(File entitiesFile) {
        return loadEntitiesFromFile(jsonTools, this.getBeanClass(), entitiesFile);
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File entitiesFile = jsonTools.getDocumentFile(rootDir, getFileName());
        if (entitiesFile == null) {
            // no file
            return;
        }
        final Map<String, B> entities = loadEntitiesFromFile(jsonTools, this.getBeanClass(), entitiesFile);
        if (entities != null) {
            entities.forEach((k,e) -> e.postLoad(k, bundle, rootDir, idGenerator));
            putToBundle(bundle, entities);
        }
    }

    @Override
    public void load(Bundle bundle, String fullName, String value) {
        String name = fullName;
        String extension = getExtension(fullName);
        String valueType = null;

        // if there is an extension, get from there
        if (isNotBlank(extension)) {
            valueType = jsonTools.getTypeFromExtension(extension);

            // if its found by extension means that is a valid extension and not part of the name
            if (valueType != null) {
                name = getBaseName(fullName);
            }
        }
        // try infering from the content
        if (valueType == null) {
            valueType = tryInferContentTypeFromValue(value);
        }
        // otherwise pick default
        if (valueType == null) {
            valueType = getDefaultValueType();
        }

        final JavaType type = jsonTools.getObjectMapper(valueType).getTypeFactory().constructType(this.getBeanClass());

        B entity = jsonTools.readStream(IOUtils.toInputStream(value, Charset.defaultCharset()), valueType, type);
        entity.postLoad(name, bundle, null, idGenerator);
        putToBundle(bundle, ImmutableMap.<String, B>builder().put(name, entity).build());
    }

    /**
     * Returns the default value type for this entity. For example JSON, YAML, etc...
     *
     * @return The default value type
     */
    protected String getDefaultValueType() {
        return JsonTools.JSON;
    }

    /**
     * @return the class of the bean loaded by the concrete implementation of this loader
     */
    protected abstract Class<B> getBeanClass();

    /**
     * @return the file name specified for this type of entity
     */
    protected abstract String getFileName();

    /**
     * Put the entities to the bundle.
     *
     * @param bundle      the bundle to add
     * @param entitiesMap non-null map of entities read from the json/yaml files
     */
    protected abstract void putToBundle(final Bundle bundle, @NotNull final Map<String, B> entitiesMap);

}
