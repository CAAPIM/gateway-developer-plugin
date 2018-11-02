/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.math.BigInteger;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert.CertificateData;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class TrustedCertLoader implements BundleDependencyLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element trustedCertElem = getSingleChildElement(getSingleChildElement(element, RESOURCE), TRUSTED_CERT);
        final String name = getSingleChildElementTextContent(trustedCertElem, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(trustedCertElem, PROPERTIES, true), PROPERTIES);

        TrustedCert cert = new TrustedCert(properties, getCertData(trustedCertElem));
        cert.setId(trustedCertElem.getAttribute(ATTRIBUTE_ID));
        cert.setName(getSingleChildElementTextContent(trustedCertElem, NAME));
        bundle.getTrustedCerts().put(name, cert);
    }

    private CertificateData getCertData(final Element trustedCertElem) {
        Element certDataElem = getSingleChildElement(trustedCertElem, CERT_DATA);

        String serialNum = getSingleChildElementTextContent(certDataElem, SERIAL_NUMBER);

        if (serialNum != null) {
            return new CertificateData(
                    getSingleChildElementTextContent(certDataElem, ISSUER_NAME),
                    new BigInteger(serialNum),
                    getSingleChildElementTextContent(certDataElem, SUBJECT_NAME),
                    getSingleChildElementTextContent(certDataElem, ENCODED)
            );
        }
        throw new DependencyBundleLoadException("Serial number of Trusted Cert must not be empty.");
    }

    @Override
    public String getEntityType() {
        return EntityTypes.TRUSTED_CERT_TYPE;
    }
}
