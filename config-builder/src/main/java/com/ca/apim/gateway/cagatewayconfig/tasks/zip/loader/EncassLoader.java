/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class EncassLoader extends EntityLoaderBase<Encass> {

    private static final String FILE_NAME = "encass";
    private final IdGenerator idGenerator;

    @Inject
    EncassLoader(final JsonTools jsonTools, final IdGenerator idGenerator) {
        super(jsonTools);
        this.idGenerator = idGenerator;
    }

    @Override
    protected Class<Encass> getBeanClass() {
        return Encass.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(final Bundle bundle, @NotNull final Map<String, Encass> entitiesMap) {
        Map<String, Encass> parsedEncasses = new HashMap<>(entitiesMap.size());
        entitiesMap.forEach((k, e) -> parsedEncasses.put(EncodeDecodeUtils.decodePath(k), e));
        parsedEncasses.values().forEach(encass -> encass.setGuid(idGenerator.generateGuid()));
        bundle.putAllEncasses(parsedEncasses);
    }

    @Override
    public String getEntityType() {
        return "ENCAPSULATED_ASSERTION";
    }
}
