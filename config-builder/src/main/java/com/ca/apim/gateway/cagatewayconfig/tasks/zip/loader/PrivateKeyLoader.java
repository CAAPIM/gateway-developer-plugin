/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;

@Singleton
public class PrivateKeyLoader extends EntityLoaderBase<PrivateKey> {

    private static final String FILE_NAME = "private-keys";

    @Inject
    PrivateKeyLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        // load private key file by standard way
        super.load(bundle, rootDir);

        // Set the private key keystore type and the certificate and key file directory
        bundle.getPrivateKeys().forEach((key, pk) -> {
            pk.setAlias(key);
            pk.setKeyStoreType(KeyStoreType.fromName(pk.getKeystore()));
            pk.setPrivateKeyDirectory(new File(rootDir, "config/privateKeys/" + key));
        });
    }

    @Override
    public String getEntityType() {
        return PRIVATE_KEY_TYPE;
    }

    @Override
    protected Class<PrivateKey> getBeanClass() {
        return PrivateKey.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, PrivateKey> entitiesMap) {
        bundle.putAllPrivateKeys(entitiesMap);
    }
}
