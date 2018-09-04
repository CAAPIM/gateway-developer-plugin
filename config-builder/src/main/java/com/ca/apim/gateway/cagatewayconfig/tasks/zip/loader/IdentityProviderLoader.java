/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chaoy01 on 2018-08-16.
 */
public class IdentityProviderLoader implements EntityLoader {
    private static final TypeReference<HashMap<String, IdentityProvider>> identityProviderTypeMapping = new TypeReference<HashMap<String, IdentityProvider>>() {
    };
    private final JsonTools jsonTools;

    public IdentityProviderLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        final Map<String, IdentityProvider> identityProviders = jsonTools.parseDocumentFile(
                new File(rootDir, "config"),
                "identity-providers",
                identityProviderTypeMapping);
        if (identityProviders != null) {
            bundle.putAllIdentityProviders(identityProviders);
        }
    }
}
