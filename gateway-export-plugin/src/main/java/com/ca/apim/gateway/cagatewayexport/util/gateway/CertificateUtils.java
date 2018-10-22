/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.gateway;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CertificateUtils {

    private static final String PEM_CERT_FILE_EXTENSION = ".pem";
    private static final String PEM_CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END_MARKER = "-----END CERTIFICATE-----";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private CertificateUtils() {}

    /**
     * Prepare the certificate encoded data to be written to a file, adding its requirements and converting to UTF-8.
     *
     * @param encodedData the raw encoded data of the certificate
     * @return byte[] containing the formatted data
     */
    public static byte[] prepareCertificateData(@NotNull String encodedData) {
        final String formattedData = encodedData.replaceAll("(.{64})", "$1" + LINE_SEPARATOR);

        return (PEM_CERT_BEGIN_MARKER +
                LINE_SEPARATOR +
                formattedData +
                LINE_SEPARATOR +
                PEM_CERT_END_MARKER)
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Concatenate the certificate name with the standard extension.
     *
     * @param certName name of the certificate
     * @return formatted name with the 'pem' extension
     */
    public static String buildCertificateFileName(@NotNull String certName) {
        return certName + PEM_CERT_FILE_EXTENSION;
    }

    /**
     * Write the certificate data to the folder specified, naming by the name specified.
     *
     * @param certFolder folder to be written into
     * @param certName name of the certificate which will be the certificate file name
     * @param certEncodedData encoded data of the certificate
     */
    public static void writeCertificateData(@NotNull final File certFolder, @NotNull final String certName, @NotNull final String certEncodedData) {
        String certFileName = CertificateUtils.buildCertificateFileName(certName);
        File certFile = new File(certFolder, certFileName);
        try (OutputStream fileStream = Files.newOutputStream(certFile.toPath())) {
            fileStream.write(CertificateUtils.prepareCertificateData(certEncodedData));
        } catch (IOException e) {
            throw new WriteException("Exception writing " + certFileName, e);
        }
    }
}
