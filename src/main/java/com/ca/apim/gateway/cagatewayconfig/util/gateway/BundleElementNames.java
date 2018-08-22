package com.ca.apim.gateway.cagatewayconfig.util.gateway;

/**
 * Utility class to hold element names used in gateway bundles.
 */
public interface BundleElementNames {

    // Common Elements
    String NAME = "l7:Name";
    String PROPERTIES = "l7:Properties";
    String PROPERTY = "l7:Property";
    String INT_VALUE = "l7:IntValue";
    String LONG_VALUE = "l7:LongValue";
    String BOOLEAN_VALUE = "l7:BooleanValue";
    String STRING_VALUE = "l7:StringValue";
    String RESOURCE = "l7:Resource";

    // Listen Port Elements
    String LISTEN_PORT = "l7:ListenPort";
    String ENABLED = "l7:Enabled";
    String PROTOCOL = "l7:Protocol";
    String PORT = "l7:Port";
    String ENABLED_FEATURES = "l7:EnabledFeatures";
    String TARGET_SERVICE_REFERENCE = "l7:TargetServiceReference";
    String TLS_SETTINGS = "l7:TlsSettings";
    String CLIENT_AUTHENTICATION = "l7:ClientAuthentication";
    String ENABLED_VERSIONS = "l7:EnabledVersions";
    String ENABLED_CIPHER_SUITES = "l7:EnabledCipherSuites";
}
