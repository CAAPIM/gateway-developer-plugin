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
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EncassLoader implements EntityLoader {
    private static final TypeReference<HashMap<String, Encass>> encassMapTypeMapping = new TypeReference<>() {
    };
    private final JsonTools jsonTools;
    private final IdGenerator idGenerator;

    public EncassLoader(JsonTools jsonTools, IdGenerator idGenerator) {
        this.jsonTools = jsonTools;
        this.idGenerator = idGenerator;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final Map<String, Encass> encasses = jsonTools.parseDocumentFile(new File(rootDir, "config"), "encass", encassMapTypeMapping);
        if (encasses != null) {
            encasses.values().forEach(encass -> encass.setGuid(idGenerator.generateGuid()));
            bundle.putAllEncasses(encasses);
        }
    }
}
