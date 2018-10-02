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
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.TRUSTED_CERT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class TrustedCertEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1100;

    private final IdGenerator idGenerator;
    private final SSLSocketFactory acceptAllSocketFactory;

    @Inject
    TrustedCertEntityBuilder(IdGenerator idGenerator, SSLSocketFactory acceptAllSocketFactory) {
        this.idGenerator = idGenerator;
        this.acceptAllSocketFactory = acceptAllSocketFactory;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return bundle.getTrustedCerts().entrySet().stream()
                        .map(
                                trustedCertEntry -> getEntityWithOnlyMapping(TRUSTED_CERT_TYPE, trustedCertEntry.getKey(), idGenerator.generate())
                        ).collect(Collectors.toList());
            case ENVIRONMENT:
                return bundle.getTrustedCerts().entrySet().stream().map(trustedCertEntry ->
                        buildTrustedCertEntity(trustedCertEntry.getKey(), trustedCertEntry.getValue(), bundle.getCertificateFiles(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    private Entity buildTrustedCertEntity(String name, TrustedCert trustedCert, Map<String, File> certificateFiles, Document document) {
        final String id = idGenerator.generate();
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                buildCertData(name, trustedCert, certificateFiles, document)
        );
        buildAndAppendPropertiesElement(trustedCert.createProperties(), document, trustedCertElem);

        return getEntityWithNameMapping(TRUSTED_CERT_TYPE, name, id, trustedCertElem);
    }

    private Element buildCertData(String name, TrustedCert trustedCert, Map<String, File> certificateFiles, Document document) {
        if (name.startsWith("https://")) {
            return buildCertDataFromUrl(name, document);
        } else if (certificateFiles.get(name) != null) {
            return buildCertDataFromFile(certificateFiles.get(name), document);
        } else if (trustedCert.getCertificateData() != null) {
            final CertificateData certData = trustedCert.getCertificateData();
            return createCertDataElementFromCert(
                    certData.getIssuerName(),
                    certData.getSerialNumber(),
                    certData.getSubjectName(),
                    certData.getEncodedData(),
                    document
            );
        } else {
            throw new EntityBuilderException("Trusted Cert must be loaded from a specified url," +
                    " or from a certificate file that has the same name as the Trusted Cert.");
        }
    }

    private Element buildCertDataFromFile(File certFileLocation, Document document) {
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
                    Base64.getEncoder().encodeToString(cert.getEncoded()),
                    document
            );
        } catch (IOException e) {
            throw new EntityBuilderException("The certificate file location specified does not exist.");
        } catch (CertificateException e) {
            throw new EntityBuilderException("Error generating certificate: ", e);
        }
    }

    private Element buildCertDataFromUrl(String name, Document document) {
        final URL url = getUrl(name);
        final int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
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
                            Base64.getEncoder().encodeToString(cert.getEncoded()),
                            document
                    );
                } else {
                    throw new EntityBuilderException("Certificate from url is not in X.509 format.");
                }
            }
            throw new EntityBuilderException("No certificates were found in the given url.");
        }
         catch (IOException | CertificateEncodingException e) {
            throw new EntityBuilderException(e.getMessage());
        }
    }

    @NotNull
    private URL getUrl(String name) {
        final URL url;
        try {
            url = new URL(name);
        } catch (MalformedURLException e) {
            throw new EntityBuilderException("The url specified is malformed: " + e.getMessage(), e);
        }
        return url;
    }

    private Element createCertDataElementFromCert(String issuerName, BigInteger serialNumber, String subjectName, String encodedData, Document document) {
        return createElementWithChildren(
                document,
                CERT_DATA,
                createElementWithTextContent(document, ISSUER_NAME, issuerName),
                createElementWithTextContent(document, SERIAL_NUMBER, serialNumber),
                createElementWithTextContent(document, SUBJECT_NAME, subjectName),
                createElementWithTextContent(document, ENCODED, encodedData)
        );
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
