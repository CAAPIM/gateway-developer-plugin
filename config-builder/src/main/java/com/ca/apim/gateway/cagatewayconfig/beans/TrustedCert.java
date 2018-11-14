/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.ImmutableMap;

import javax.inject.Named;
import java.io.File;
import java.math.BigInteger;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.writeCertificateData;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@JsonInclude(Include.NON_NULL)
@Named("TRUSTED_CERT")
@ConfigurationFile(name = "trusted-certs", type = JSON_YAML)
@EnvironmentType("CERTIFICATE")
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
        this(firstNonNull(properties, emptyMap()));
        this.certificateData = certificateData;
    }

    private TrustedCert(final Builder builder) {
        this(builder.properties, new CertificateData(builder.encodedData));
        setId(builder.id);
        setName(builder.name);
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

        public CertificateData(String encodedData) {
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

    public static class Builder {
        private String id;
        private String name;
        private Map<String, Object> properties;
        private String encodedData;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder encodedData(String encodedData) {
            this.encodedData = encodedData;
            return this;
        }

        public TrustedCert build() {
            return new TrustedCert(this);
        }
    }

    @Override
    public void preWrite(File configFolder, DocumentFileUtils documentFileUtils) {
        final File certFolder = new File(configFolder, "certificates");
        documentFileUtils.createFolder(certFolder.toPath());

        writeCertificateData(certFolder, getName(), getCertificateData().getEncodedData());

        // remove the certificate data so it dont get written to the file
        this.certificateData = null;
    }
}
