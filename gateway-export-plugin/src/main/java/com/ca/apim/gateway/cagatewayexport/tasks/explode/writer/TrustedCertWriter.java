/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;

@Singleton
public class TrustedCertWriter implements EntityWriter {

    private static final String TRUSTED_CERTS_FILE = "trusted-certs";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    TrustedCertWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, TrustedCert> trustedCertBeans = bundle.getEntities(TrustedCertEntity.class)
                .values()
                .stream()
                .collect(Collectors.toMap(TrustedCertEntity::getName, (v -> getTrustedCertBean(v, rootFolder))));

        writeFile(rootFolder, documentFileUtils, jsonTools, trustedCertBeans, TRUSTED_CERTS_FILE, TrustedCert.class);
    }

    private TrustedCert getTrustedCertBean(final TrustedCertEntity trustedCertEntity, File rootFolder) {
        final TrustedCert trustedCert =  new TrustedCert(trustedCertEntity.getProperties());
        writeCertFile(trustedCertEntity, rootFolder);
        return trustedCert;
    }

    private void writeCertFile(TrustedCertEntity trustedCertEntity, File rootFolder) {
        final File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());
        final File certFolder = new File(configFolder, "certificates");
        documentFileUtils.createFolder(certFolder.toPath());

        CertificateUtils.writeCertificateData(certFolder, trustedCertEntity.getName(), trustedCertEntity.getEncodedData());
    }
}
