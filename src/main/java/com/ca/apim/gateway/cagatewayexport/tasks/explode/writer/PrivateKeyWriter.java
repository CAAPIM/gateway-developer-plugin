/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PrivateKeyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.IntStream;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static com.ca.apim.gateway.cagatewayexport.util.gateway.CertificateUtils.writeCertificateData;
import static java.util.stream.Collectors.toMap;

@Singleton
public class PrivateKeyWriter implements EntityWriter {

    private static final String FILE_NAME = "private-keys";
    private static final String CERT_FILE_NAME_FORMAT = "%s_certificate";

    private final DecimalFormat decimalFormat;
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    public PrivateKeyWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
        this.decimalFormat = new DecimalFormat("00");
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, PrivateKey> privateKeys = bundle.getEntities(PrivateKeyEntity.class)
                .values()
                .stream()
                .collect(toMap(PrivateKeyEntity::getName, entity -> getPrivateKeyBean(entity, rootFolder)));

        writeFile(rootFolder, documentFileUtils, jsonTools, privateKeys, FILE_NAME, PrivateKey.class);
    }

    private PrivateKey getPrivateKeyBean(PrivateKeyEntity entity, File rootFolder) {
        writeCertificateChain(entity, rootFolder);

        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlgorithm(entity.getAlgorithm());
        privateKey.setKeystore(entity.getKeystore().getName());
        return privateKey;
    }

    private void writeCertificateChain(PrivateKeyEntity privateKey, File rootFolder) {
        // Create a folder structure to store the key (user will have to do this manually)
        // and a subfolder where the certificates of the chain will be stored
        final File certificateChainFolder = new File(rootFolder, "config/privateKeys/" + privateKey.getName() + "/certificateChain");
        documentFileUtils.createFolders(certificateChainFolder.toPath());

        // iterate the certificates and write them to the certificate directory
        IntStream.range(0, privateKey.getCertificateChainData().size())
                .forEach(i -> writeCertificate(certificateChainFolder, privateKey.getCertificateChainData().get(i), i));
    }

    private void writeCertificate(File certificateChainFolder, String certData, int index) {
        writeCertificateData(certificateChainFolder, String.format(CERT_FILE_NAME_FORMAT, decimalFormat.format(index)), certData);
    }
}
