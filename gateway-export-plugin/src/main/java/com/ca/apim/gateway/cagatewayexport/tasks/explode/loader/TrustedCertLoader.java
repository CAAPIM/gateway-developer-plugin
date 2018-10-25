/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class TrustedCertLoader implements EntityLoader<TrustedCertEntity> {
    @Override
    public TrustedCertEntity load(Element element) {
        final Element trustedCertElem = getSingleChildElement(getSingleChildElement(element, RESOURCE), TRUSTED_CERT);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(trustedCertElem, PROPERTIES, true), PROPERTIES);

        return new TrustedCertEntity.Builder()
                .id(trustedCertElem.getAttribute(ATTRIBUTE_ID))
                .name(getSingleChildElementTextContent(trustedCertElem, NAME))
                .properties(properties)
                .encodedData(getSingleChildElementTextContent(getSingleChildElement(trustedCertElem, CERT_DATA), ENCODED))
                .build();
    }

    @Override
    public Class<TrustedCertEntity> entityClass() {
        return TrustedCertEntity.class;
    }
}
