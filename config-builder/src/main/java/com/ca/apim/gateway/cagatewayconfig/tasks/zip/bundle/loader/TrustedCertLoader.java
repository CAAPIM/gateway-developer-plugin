/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import org.w3c.dom.Element;

import java.math.BigInteger;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

public class TrustedCertLoader implements BundleEntityLoader {
    @Override
    public void load(Bundle bundle, Element element) {
        final Element trustedCertElem = getSingleChildElement(getSingleChildElement(element, RESOURCE), TRUSTED_CERT);
        final String name = getSingleChildElementTextContent(trustedCertElem, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(trustedCertElem, PROPERTIES, true), PROPERTIES);

        bundle.getTrustedCerts().put(name, new TrustedCert(properties, getCertData(trustedCertElem)));
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
}
