/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert.CertificateData;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class TrustedCertLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element trustedCertElem = getSingleChildElement(getSingleChildElement(element, RESOURCE), TRUSTED_CERT);
        final String name = getSingleChildElementTextContent(trustedCertElem, NAME);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(trustedCertElem, PROPERTIES, true), PROPERTIES);

        TrustedCert cert = new TrustedCert(properties, getCertData(trustedCertElem));
        cert.setId(trustedCertElem.getAttribute(ATTRIBUTE_ID));
        cert.setName(getSingleChildElementTextContent(trustedCertElem, NAME));
        Set<Annotation> annotations = new HashSet<>();
        Annotation bundleEntity = new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE_ENTITY);
        bundleEntity.setId(trustedCertElem.getAttribute(ATTRIBUTE_ID));
        annotations.add(bundleEntity);
        cert.setAnnotations(annotations);
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
        throw new BundleLoadException("Serial number of Trusted Cert must not be empty.");
    }

    @Override
    public String getEntityType() {
        return EntityTypes.TRUSTED_CERT_TYPE;
    }
}
