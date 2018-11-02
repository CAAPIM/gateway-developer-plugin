/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.google.common.collect.ImmutableMap;

import javax.inject.Named;
import java.math.BigInteger;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static java.lang.Boolean.parseBoolean;

@Named("TRUSTED_CERT")
public class TrustedCert extends GatewayEntity {

    private boolean verifyHostname;
    private boolean trustedForSsl;
    private boolean trustedAsSamlAttestingEntity;
    private boolean trustAnchor;
    private boolean revocationCheckingEnabled;
    private boolean trustedForSigningClientCerts;
    private boolean trustedForSigningServerCerts;
    private boolean trustedAsSamlIssuer;
    private CertificateData certificateData;

    public TrustedCert() {}

    public TrustedCert(Map<String, Object> properties, CertificateData certificateData) {
        this(properties);
        this.certificateData = certificateData;
    }

    public TrustedCert(Map<String, Object> properties) {
        verifyHostname = extractBoolean(properties, VERIFY_HOSTNAME);
        trustedForSsl = extractBoolean(properties, TRUSTED_FOR_SSL);
        trustedAsSamlAttestingEntity = extractBoolean(properties, TRUSTED_AS_SAML_ATTESTING_ENTITY);
        trustAnchor = extractBoolean(properties, TRUST_ANCHOR);
        revocationCheckingEnabled = extractBoolean(properties, REVOCATION_CHECKING_ENABLED);
        trustedForSigningClientCerts = extractBoolean(properties, TRUSTING_SIGNING_CLIENT_CERTS);
        trustedForSigningServerCerts = extractBoolean(properties, TRUSTED_SIGNING_SERVER_CERTS);
        trustedAsSamlIssuer = extractBoolean(properties, TRUSTED_AS_SAML_ISSUER);
    }

    public Map<String, Object> createProperties() {
        return ImmutableMap.<String, Object>builder()
                .put(VERIFY_HOSTNAME, verifyHostname)
                .put(TRUSTED_FOR_SSL, trustedForSsl)
                .put(TRUSTED_AS_SAML_ATTESTING_ENTITY, trustedAsSamlAttestingEntity)
                .put(TRUST_ANCHOR, trustAnchor)
                .put(REVOCATION_CHECKING_ENABLED, revocationCheckingEnabled)
                .put(TRUSTING_SIGNING_CLIENT_CERTS, trustedForSigningClientCerts)
                .put(TRUSTED_SIGNING_SERVER_CERTS, trustedForSigningServerCerts)
                .put(TRUSTED_AS_SAML_ISSUER, trustedAsSamlIssuer)
                .build();
    }

    public void setCertificateData(CertificateData certificateData) {
        this.certificateData = certificateData;
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

    private boolean extractBoolean(Map<String, Object> properties, String keyName) {
        return properties.get(keyName) != null && parseBoolean(properties.get(keyName).toString());
    }

    public boolean isVerifyHostname() {
        return verifyHostname;
    }

    public boolean isTrustedForSsl() {
        return trustedForSsl;
    }

    public boolean isTrustedAsSamlAttestingEntity() {
        return trustedAsSamlAttestingEntity;
    }

    public boolean isTrustAnchor() {
        return trustAnchor;
    }

    public boolean isRevocationCheckingEnabled() {
        return revocationCheckingEnabled;
    }

    public boolean isTrustedForSigningClientCerts() {
        return trustedForSigningClientCerts;
    }

    public boolean isTrustedForSigningServerCerts() {
        return trustedForSigningServerCerts;
    }

    public boolean isTrustedAsSamlIssuer() {
        return trustedAsSamlIssuer;
    }
}
