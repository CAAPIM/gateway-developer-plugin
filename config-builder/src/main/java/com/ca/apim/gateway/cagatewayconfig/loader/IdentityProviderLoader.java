/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Created by chaoy01 on 2018-08-16.
 */
@Singleton
public class IdentityProviderLoader extends EntityLoaderBase<IdentityProvider> {

    private static final String FILE_NAME = "identity-providers";

    @Inject
    IdentityProviderLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<IdentityProvider> getBeanClass() {
        return IdentityProvider.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, IdentityProvider> entitiesMap) {
        bundle.putAllIdentityProviders(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "IDENTITY_PROVIDER";
    }
}
