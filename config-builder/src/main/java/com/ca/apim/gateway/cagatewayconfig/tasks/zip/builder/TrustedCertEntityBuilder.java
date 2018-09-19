/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert.CertificateData;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.TRUSTED_CERT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class TrustedCertEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    TrustedCertEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle) {
        return bundle.getTrustedCerts().entrySet().stream().map(trustedCertEntry ->
                buildTrustedCertEntity(trustedCertEntry.getKey(), trustedCertEntry.getValue(), bundle.getCertificateFiles())
        ).collect(Collectors.toList());
    }

    private Entity buildTrustedCertEntity(String name, TrustedCert trustedCert, Map<String, String> certificateFiles) {
        final String id = idGenerator.generate();
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                buildCertData(trustedCert, certificateFiles)
        );
        buildAndAppendPropertiesElement(trustedCert.getProperties(), document, trustedCertElem);

        return new Entity(TRUSTED_CERT_TYPE, name, id, trustedCertElem);
    }

    private Element buildCertData(TrustedCert trustedCert, Map<String, String> certificateFiles) {
        if (StringUtils.isNotEmpty(trustedCert.getUrl())) {
            return buildCertDataFromUrl(trustedCert);
        } else if (StringUtils.isNotEmpty(trustedCert.getFile())) {
            return buildCertDataFromFile(certificateFiles.get(trustedCert.getFile()));
        } else if (trustedCert.getCertificateData() != null) {
            CertificateData certData = trustedCert.getCertificateData();
            return createCertDataElementFromCert(
                    certData.getIssuerName(),
                    certData.getSerialNumber(),
                    certData.getSubjectName(),
                    certData.getEncodedData()
            );
        } else {
            throw new EntityBuilderException("Trusted Cert must be loaded from a valid url, from a file, or using certificateData reference.");
        }
    }

    private Element buildCertDataFromFile(String certFileLocation) {
        if (StringUtils.isEmpty(certFileLocation)) {
            throw new EntityBuilderException("The certificate file location is not specified.");
        }

        try (FileInputStream is = new FileInputStream(certFileLocation)) {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFact.generateCertificate(is);
            return createCertDataElementFromCert(
                    cert.getIssuerDN().getName(),
                    cert.getSerialNumber(),
                    cert.getSubjectDN().getName(),
                    Base64.getEncoder().encodeToString(cert.getEncoded())
            );
        } catch (IOException e) {
            throw new EntityBuilderException("The certificate file location specified does not exist.");
        } catch (CertificateException e) {
            throw new EntityBuilderException("Error generating certificate: ", e);
        }
    }

    private Element buildCertDataFromUrl(TrustedCert trustedCert) {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(trustedCert.getUrl());
            conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            if (certs.length > 0) {
                //Just add leaf cert if a chain is presented
                if (certs[0] instanceof X509Certificate) {
                    final X509Certificate cert = (X509Certificate) certs[0];
                    return createCertDataElementFromCert(
                            cert.getIssuerDN().getName(),
                            cert.getSerialNumber(),
                            cert.getSubjectDN().getName(),
                            Base64.getEncoder().encodeToString(cert.getEncoded())
                    );
                } else {
                    throw new EntityBuilderException("Certificate from url is not in X.509 format.");
                }
            }
            throw new EntityBuilderException("No certificates were found in the given url.");
        } catch (IOException | CertificateEncodingException e) {
            throw new EntityBuilderException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Element createCertDataElementFromCert(String issuerName, BigInteger serialNumber, String subjectName, String encodedData) {
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
