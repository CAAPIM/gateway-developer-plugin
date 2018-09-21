/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.util.properties.PropertyConstants.*;
import static java.lang.Boolean.parseBoolean;

public class TrustedCert {

    private boolean verifyHostname;
    private boolean trustedForSsl;
    private boolean trustedAsSamlAttestingEntity;
    private boolean trustAnchor;
    private boolean revocationCheckingEnabled;
    private boolean trustedForSigningClientCerts;
    private boolean trustedForSigningServerCerts;
    private boolean trustedAsSamlIssuer;

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

    private boolean extractBoolean(Map<String, Object> properties, String keyName) {
        return properties.get(keyName) != null && parseBoolean(properties.get(keyName).toString());
    }
}
