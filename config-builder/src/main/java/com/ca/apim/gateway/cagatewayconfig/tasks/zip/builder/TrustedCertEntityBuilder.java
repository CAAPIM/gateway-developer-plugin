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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
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
    private final SSLSocketFactory acceptAllSocketFactory;

    TrustedCertEntityBuilder(Document document, IdGenerator idGenerator, SSLSocketFactory acceptAllSocketFactory) {
        this.document = document;
        this.idGenerator = idGenerator;
        this.acceptAllSocketFactory = acceptAllSocketFactory;
    }

    @Override
    public List<Entity> build(Bundle bundle) {
        return bundle.getTrustedCerts().entrySet().stream().map(trustedCertEntry ->
                buildTrustedCertEntity(trustedCertEntry.getKey(), trustedCertEntry.getValue(), bundle.getCertificateFiles())
        ).collect(Collectors.toList());
    }

    private Entity buildTrustedCertEntity(String name, TrustedCert trustedCert, Map<String, File> certificateFiles) {
        final String id = idGenerator.generate();
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                buildCertData(name, trustedCert, certificateFiles)
        );
        buildAndAppendPropertiesElement(trustedCert.createProperties(), document, trustedCertElem);

        return new Entity(TRUSTED_CERT_TYPE, name, id, trustedCertElem);
    }

    private Element buildCertData(String name, TrustedCert trustedCert, Map<String, File> certificateFiles) {
        if (name.startsWith("https://")) {
            return buildCertDataFromUrl(name);
        } else if (certificateFiles.get(name) != null) {
            return buildCertDataFromFile(certificateFiles.get(name));
        } else if (trustedCert.getCertificateData() != null) {
            final CertificateData certData = trustedCert.getCertificateData();
            return createCertDataElementFromCert(
                    certData.getIssuerName(),
                    certData.getSerialNumber(),
                    certData.getSubjectName(),
                    certData.getEncodedData()
            );
        } else {
            throw new EntityBuilderException("Trusted Cert must be loaded from a specified url," +
                    " or from a certificate file that has the same name as the Trusted Cert.");
        }
    }

    private Element buildCertDataFromFile(File certFileLocation) {
        if (!certFileLocation.exists()) {
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

    private Element buildCertDataFromUrl(String name) {
        try {
            final URL url = new URL(name);
            int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
            try (SSLSocket socket = (SSLSocket) acceptAllSocketFactory.createSocket(url.getHost(), port)) {
                socket.startHandshake();
                Certificate[] certs = socket.getSession().getPeerCertificates();
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
            }
        } catch (MalformedURLException e) {
            throw new EntityBuilderException("The url specified is malformed: " + e.getMessage(), e);
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
