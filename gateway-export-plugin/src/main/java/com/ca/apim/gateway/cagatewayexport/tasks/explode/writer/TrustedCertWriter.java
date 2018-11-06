/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collection;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.writeCertificateData;
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
        writeCertFiles(bundle.getTrustedCerts().values(), rootFolder);

        writeFile(rootFolder, documentFileUtils, jsonTools, bundle.getEntities(TrustedCert.class), TRUSTED_CERTS_FILE, TrustedCert.class);
    }

    private void writeCertFiles(Collection<TrustedCert> trustedCerts, File rootFolder) {
        final File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());
        final File certFolder = new File(configFolder, "certificates");
        documentFileUtils.createFolder(certFolder.toPath());

        trustedCerts.forEach(trustedCertEntity -> writeCertificateData(certFolder, trustedCertEntity.getName(), trustedCertEntity.getCertificateData().getEncodedData()));
    }
}
