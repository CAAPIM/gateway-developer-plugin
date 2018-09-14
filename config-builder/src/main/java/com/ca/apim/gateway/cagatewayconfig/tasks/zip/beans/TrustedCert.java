/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.math.BigInteger;
import java.util.Map;

public class TrustedCert {

    private Map<String, Object> properties;
    private CertificateData certificateData;

    public TrustedCert() {}

    public TrustedCert(Map<String, Object> properties, CertificateData certificateData) {
        this.properties = properties;
        this.certificateData = certificateData;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setCertificateData(CertificateData certificateData) {
        this.certificateData = certificateData;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public CertificateData getCertificateData() {
        return certificateData;
    }

    public static class CertificateData {
        private String issuerName;
        private BigInteger serialNumber;
        private String subjectName;
        private String encodedData;

        public CertificateData() {}

        public CertificateData(String issuerName, BigInteger serialNumber, String subjectName, String encodedData) {
            this.issuerName = issuerName;
            this.serialNumber = serialNumber;
            this.subjectName = subjectName;
            this.encodedData = encodedData;
        }

        public String getIssuerName() {
            return issuerName;
        }

        public BigInteger getSerialNumber() {
            return serialNumber;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getEncodedData() {
            return encodedData;
        }
    }

}
