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

import java.util.List;
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
                buildTrustedCertEntity(trustedCertEntry.getKey(), trustedCertEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildTrustedCertEntity(String name, TrustedCert trustedCert) {
        final String id = idGenerator.generate();
        final Element trustedCertElem = createElementWithAttributesAndChildren(
                document,
                TRUSTED_CERT,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                buildCertData(trustedCert.getCertificateData())
        );
        buildAndAppendPropertiesElement(trustedCert.getProperties(), document, trustedCertElem);

        return new Entity(TRUSTED_CERT_TYPE, name, id, trustedCertElem);

    }

    private Element buildCertData(CertificateData certificateData) {
        return createElementWithChildren(
                document,
                CERT_DATA,
                createElementWithTextContent(document, ISSUER_NAME, certificateData.getIssuerName()),
                createElementWithTextContent(document, SERIAL_NUMBER, certificateData.getSerialNumber()),
                createElementWithTextContent(document, SUBJECT_NAME, certificateData.getSubjectName()),
                createElementWithTextContent(document, ENCODED, certificateData.getEncodedData())
        );
    }
}
