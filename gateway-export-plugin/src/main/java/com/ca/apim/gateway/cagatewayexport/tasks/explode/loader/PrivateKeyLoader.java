/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.KeyStoreType;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PrivateKeyEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.*;
import static java.util.stream.Collectors.toList;

@Singleton
public class PrivateKeyLoader implements EntityLoader<PrivateKeyEntity> {

    @Override
    public PrivateKeyEntity load(Element element) {
        final Element privateKey = getSingleChildElement(getSingleChildElement(element, RESOURCE), PRIVATE_KEY);

        final String alias = privateKey.getAttribute(ATTRIBUTE_ALIAS);
        final KeyStoreType keystore = KeyStoreType.fromId(privateKey.getAttribute(ATTRIBUTE_KEYSTORE_ID));
        final List<String> certificateChain = getChildElements(getSingleChildElement(privateKey, CERTIFICATE_CHAIN), CERT_DATA)
                .stream()
                .map(e -> getSingleChildElementTextContent(e, ENCODED))
                .collect(toList());
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(privateKey, PROPERTIES), PROPERTIES);

        return new PrivateKeyEntity.Builder()
                .setId(keystore.generateKeyId(alias))
                .setAlias(alias)
                .setKeystore(keystore)
                .setAlgorithm((String) properties.get("keyAlgorithm"))
                .setCertificateChainData(certificateChain)
                .build();
    }

    @Override
    public Class<PrivateKeyEntity> entityClass() {
        return PrivateKeyEntity.class;
    }
}
