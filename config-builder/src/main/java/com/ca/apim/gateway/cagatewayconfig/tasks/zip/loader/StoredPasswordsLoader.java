/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class StoredPasswordsLoader extends PropertiesLoaderBase {

    private static final String STORED_PASSWORDS_PROPERTIES = "config/stored-passwords.properties";

    StoredPasswordsLoader(FileUtils fileUtils) {
        super(fileUtils);
    }

    @Override
    protected String getFilePath() {
        return STORED_PASSWORDS_PROPERTIES;
    }

    @Override
    protected void putToBundle(Bundle bundle, Map<String, String> properties) {
        bundle.putAllStoredPasswords(properties.entrySet().stream().map(e -> buildStoredPassword(e.getKey(), e.getValue())).collect(toMap(StoredPassword::getName, identity())));
    }

    private StoredPassword buildStoredPassword(String name, String password) {
        if (isEmpty(password)) {
            throw new BundleLoadException("Stored passwords file contains an empty password: " + name);
        }

        StoredPassword storedPassword = new StoredPassword();
        storedPassword.setName(name);
        storedPassword.setPassword(password);
        storedPassword.addDefaultProperties();
        return storedPassword;
    }
}
