/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert.CertificateData;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.TRUSTED_CERT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.buildCertDataFromFile;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.createCertDataElementFromCert;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class TrustedCertEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 700;

    private final IdGenerator idGenerator;
    private final SSLSocketFactory acceptAllSocketFactory;
    private final CertificateFactory certFactory;

    @Inject
    TrustedCertEntityBuilder(IdGenerator idGenerator, SSLSocketFactory acceptAllSocketFactory, CertificateFactory certFactory) {
        this.idGenerator = idGenerator;
        this.acceptAllSocketFactory = acceptAllSocketFactory;
        this.certFactory = certFactory;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        if (bundle instanceof AnnotatedBundle) {
            Map<String, TrustedCert> trustedCertMap = Optional.ofNullable(bundle.getTrustedCerts()).orElse(Collections.emptyMap());
            return buildEntities(trustedCertMap, ((AnnotatedBundle)bundle).getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getTrustedCerts(), bundle, bundleType, document);
        }
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return entities.entrySet().stream()
                        .map(
                                trustedCertEntry -> EntityBuilderHelper.getEntityWithOnlyMapping(TRUSTED_CERT_TYPE, trustedCertEntry.getKey(),
                                        ((TrustedCert)trustedCertEntry.getValue()).getAnnotatedEntity() !=null && ((TrustedCert)trustedCertEntry.getValue()).getAnnotatedEntity().getId() != null ?
                                        ((TrustedCert)trustedCertEntry.getValue()).getAnnotatedEntity().getId() : idGenerator.generate())
                        ).collect(Collectors.toList());
            case ENVIRONMENT:
                return entities.entrySet().stream().map(trustedCertEntry ->
                        buildTrustedCertEntity(trustedCertEntry.getKey(), (TrustedCert) trustedCertEntry.getValue(), bundle.getCertificateFiles(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    private Entity buildTrustedCertEntity(String name, TrustedCert trustedCert, Map<String, SupplierWithIO<InputStream>> certificateFiles, Document document) {
        final String id = trustedCert.getAnnotatedEntity() != null && trustedCert.getAnnotatedEntity().getId() != null ? trustedCert.getAnnotatedEntity().getId(): idGenerator.generate();
        trustedCert.setId(id);
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                buildCertData(name, trustedCert, certificateFiles, document)
        );
        buildAndAppendPropertiesElement(trustedCert.createProperties(), document, trustedCertElem);

        return EntityBuilderHelper.getEntityWithNameMapping(TRUSTED_CERT_TYPE, name, id, trustedCertElem);
    }

    private Element buildCertData(String name, TrustedCert trustedCert, Map<String, SupplierWithIO<InputStream>> certificateFiles, Document document) {
        if (name.startsWith("https://")) {
            return buildCertDataFromUrl(name, document);
        } else if (certificateFiles.get(name) != null) {
            return buildCertDataFromFile(certificateFiles.get(name), document, certFactory);
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
        } catch (IOException | CertificateEncodingException e) {
            throw new EntityBuilderException(e.getMessage());
        }
    }

    @NotNull
    @VisibleForTesting
    URL getUrl(String name) {
        final URL url;
        try {
            url = new URL(name);
        } catch (MalformedURLException e) {
            throw new EntityBuilderException("The url specified is malformed: " + e.getMessage(), e);
        }
        return url;
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
