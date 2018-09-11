/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.util.gateway;

/**
 * Utility class to hold element names used in gateway bundles.
 */
public class BundleElementNames {

    // Attributes
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_KEY = "key";
    public static final String ATTRIBUTE_RESOURCE_URI= "resourceUri";


    // Common Elements
    public static final String NAME = "l7:Name";
    public static final String PROPERTIES = "l7:Properties";
    public static final String PROPERTY = "l7:Property";
    public static final String INT_VALUE = "l7:IntValue";
    public static final String LONG_VALUE = "l7:LongValue";
    public static final String BOOLEAN_VALUE = "l7:BooleanValue";
    public static final String STRING_VALUE = "l7:StringValue";
    public static final String RESOURCE = "l7:Resource";
    public static final String REFERENCE = "l7:Reference";

    // Listen Port Elements
    public static final String LISTEN_PORT = "l7:ListenPort";
    public static final String ENABLED = "l7:Enabled";
    public static final String PROTOCOL = "l7:Protocol";
    public static final String PORT = "l7:Port";
    public static final String ENABLED_FEATURES = "l7:EnabledFeatures";
    public static final String TARGET_SERVICE_REFERENCE = "l7:TargetServiceReference";
    public static final String TLS_SETTINGS = "l7:TlsSettings";
    public static final String CLIENT_AUTHENTICATION = "l7:ClientAuthentication";
    public static final String ENABLED_VERSIONS = "l7:EnabledVersions";
    public static final String ENABLED_CIPHER_SUITES = "l7:EnabledCipherSuites";

    // Identity Provider Elements
    public static final String ID_PROV = "l7:IdentityProvider";
    public static final String ID_PROV_TYPE = "l7:IdentityProviderType";
    public static final String EXTENSION = "l7:Extension";
    public static final String SERVER_URLS = "l7:ServerUrls";
    public static final String BIND_ONLY_ID_PROV_DETAIL = "l7:BindOnlyLdapIdentityProviderDetail";
    public static final String FEDERATED_ID_PROV_DETAIL = "l7:FederatedIdentityProviderDetail";
    public static final String CERTIFICATE_REFERENCES = "l7:CertificateReferences";
    public static final String USE_SSL_CLIENT_AUTH = "l7:UseSslClientAuthentication";
    public static final String BIND_PATTERN_PREFIX = "l7:BindPatternPrefix";
    public static final String BIND_PATTERN_SUFFIX = "l7:BindPatternSuffix";

    private BundleElementNames() {
        //
    }
}
