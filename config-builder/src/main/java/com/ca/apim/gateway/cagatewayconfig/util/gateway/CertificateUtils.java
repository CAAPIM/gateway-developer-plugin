/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderException;
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class CertificateUtils {

    private CertificateUtils() {}

    public static Element buildCertDataFromFile(SupplierWithIO<InputStream> certFileLocation, Document document, CertificateFactory certificateFactory) {
        X509Certificate cert = loadCertificateFromFile(certFileLocation, certificateFactory);
        return createCertDataElementFromCert(cert, document);
    }

    public static X509Certificate loadCertificateFromFile(SupplierWithIO<InputStream> certFileLocation, CertificateFactory certificateFactory) {
        X509Certificate cert;
        try (InputStream is = certFileLocation.getWithIO()) {
            cert = (X509Certificate) certificateFactory.generateCertificate(is);
        } catch (IOException e) {
            throw new EntityBuilderException("The certificate file location specified does not exist.");
        } catch (CertificateException e) {
            throw new EntityBuilderException("Error generating certificate from file", e);
        }
        return cert;
    }

    public static Element createCertDataElementFromCert(X509Certificate cert, Document document) {
        try {
            return createCertDataElementFromCert(
                    cert.getIssuerDN().getName(),
                    cert.getSerialNumber(),
                    cert.getSubjectDN().getName(),
                    Base64.getEncoder().encodeToString(cert.getEncoded()),
                    document
            );
        } catch (CertificateEncodingException e) {
            throw new EntityBuilderException("Error generating certificate: ", e);
        }
    }

    public static Element createCertDataElementFromCert(String issuerName, BigInteger serialNumber, String subjectName, String encodedData, Document document) {
        return createElementWithChildren(
                document,
                CERT_DATA,
                createElementWithTextContent(document, ISSUER_NAME, issuerName),
                createElementWithTextContent(document, SERIAL_NUMBER, serialNumber),
                createElementWithTextContent(document, SUBJECT_NAME, subjectName),
                createElementWithTextContent(document, ENCODED, encodedData)
        );
    }

}
