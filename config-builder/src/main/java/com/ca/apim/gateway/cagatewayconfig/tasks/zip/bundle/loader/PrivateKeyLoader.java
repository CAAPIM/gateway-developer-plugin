/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class PrivateKeyLoader implements BundleDependencyLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element privateKey = getSingleChildElement(getSingleChildElement(element, RESOURCE), PRIVATE_KEY);

        final String alias = privateKey.getAttribute(ATTRIBUTE_ALIAS);
        final KeyStoreType keystore = KeyStoreType.fromId(privateKey.getAttribute(ATTRIBUTE_KEYSTORE_ID));
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(privateKey, PROPERTIES), PROPERTIES);

        PrivateKey key = new PrivateKey();
        key.setId(keystore.generateKeyId(alias));
        key.setAlias(alias);
        key.setKeyStoreType(keystore);
        key.setKeystore(keystore.getName());
        key.setAlgorithm((String) properties.get("keyAlgorithm"));

        bundle.getPrivateKeys().put(alias, key);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.PRIVATE_KEY_TYPE;
    }


}
