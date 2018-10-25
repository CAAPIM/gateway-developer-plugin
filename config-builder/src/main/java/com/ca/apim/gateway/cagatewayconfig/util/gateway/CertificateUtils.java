/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class CertificateUtils {

    private static final String PEM_CERT_FILE_EXTENSION = ".pem";
    private static final String PEM_CERT_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END_MARKER = "-----END CERTIFICATE-----";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private CertificateUtils() {}

    public static Element buildCertDataFromFile(SupplierWithIO<InputStream> certFileLocation, Document document, CertificateFactory certificateFactory) {
        X509Certificate cert = loadCertificateFromFile(certFileLocation, certificateFactory);
        return createCertDataElementFromCert(cert, document);
    }

    private static X509Certificate loadCertificateFromFile(SupplierWithIO<InputStream> certFileLocation, CertificateFactory certificateFactory) {
        X509Certificate cert;
        try (InputStream is = certFileLocation.getWithIO()) {
            cert = (X509Certificate) certificateFactory.generateCertificate(is);
        } catch (IOException e) {
            throw new CertificateUtilsException("The certificate file location specified does not exist.");
        } catch (CertificateException e) {
            throw new CertificateUtilsException("Error generating certificate from file", e);
        }
        return cert;
    }

    public static Element createCertDataElementFromCert(X509Certificate cert, Document document) {
        try {
            return createCertDataElementFromCert(
                    cert.getIssuerDN().getName(),
                    cert.getSerialNumber(),
                    cert.getSubjectDN().getName(),
                    Base64.getEncoder().encodeToString(cert.getEncoded()),
                    document
            );
        } catch (CertificateEncodingException e) {
            throw new CertificateUtilsException("Error generating certificate: ", e);
        }
    }

    public static Element createCertDataElementFromCert(String issuerName, BigInteger serialNumber, String subjectName, String encodedData, Document document) {
        return createElementWithChildren(
                document,
                CERT_DATA,
                createElementWithTextContent(document, ISSUER_NAME, issuerName),
                createElementWithTextContent(document, SERIAL_NUMBER, serialNumber),
                createElementWithTextContent(document, SUBJECT_NAME, subjectName),
                createElementWithTextContent(document, ENCODED, encodedData)
        );
    }


    /**
     * Prepare the certificate encoded data to be written to a file, adding its requirements and converting to UTF-8.
     *
     * @param encodedData the raw encoded data of the certificate
     * @return byte[] containing the formatted data
     */
    private static byte[] prepareCertificateData(@NotNull String encodedData) {
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
    private static String buildCertificateFileName(@NotNull String certName) {
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
        String certFileName = buildCertificateFileName(certName);
        File certFile = new File(certFolder, certFileName);
        try (OutputStream fileStream = Files.newOutputStream(certFile.toPath())) {
            fileStream.write(prepareCertificateData(certEncodedData));
        } catch (IOException e) {
            throw new CertificateUtilsException("Exception writing " + certFileName, e);
        }
    }
}
