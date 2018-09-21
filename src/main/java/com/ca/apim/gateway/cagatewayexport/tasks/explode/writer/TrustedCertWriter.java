/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;

@Singleton
public class TrustedCertWriter implements EntityWriter {
    private static final String TRUSTED_CERTS_FILE = "trusted-certs";
    private static final String PEM_CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END_MARKER = "-----END CERTIFICATE-----";
    private static final String LINE_SEPERATOR = System.lineSeparator();
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

        writeFile(rootFolder, documentFileUtils, jsonTools, trustedCertBeans, TRUSTED_CERTS_FILE);
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

        final String formattedData = trustedCertEntity.getEncodedData().replaceAll("(.{64})", "$1" + LINE_SEPERATOR);

        File certFile = new File(certFolder, trustedCertEntity.getName() + ".pem");
        try (OutputStream fileStream = Files.newOutputStream(certFile.toPath())) {
            fileStream.write(PEM_CERT_BEGIN_MARKER.getBytes(StandardCharsets.UTF_8));
            fileStream.write(LINE_SEPERATOR.getBytes(StandardCharsets.UTF_8));
            fileStream.write(formattedData.getBytes(StandardCharsets.UTF_8));
            fileStream.write(LINE_SEPERATOR.getBytes(StandardCharsets.UTF_8));
            fileStream.write(PEM_CERT_END_MARKER.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new WriteException("Exception writing " + trustedCertEntity.getName() + ".pem", e);
        }
    }
}
