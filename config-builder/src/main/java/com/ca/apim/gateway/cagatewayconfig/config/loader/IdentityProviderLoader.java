/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
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
    private IdGenerator idGenerator;

    @Inject
    IdentityProviderLoader(final JsonTools jsonTools, final IdGenerator idGenerator) {
        super(jsonTools);
        this.idGenerator = idGenerator;
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
        entitiesMap.values().forEach(idProv -> idProv.setId(idGenerator.generate()));
        bundle.putAllIdentityProviders(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "IDENTITY_PROVIDER";
    }
}
