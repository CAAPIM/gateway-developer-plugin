/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

/**
 * Utility class to hold element names used in gateway bundles.
 */
public class BundleElementNames {

    // Common Elements
    public static final String NAME = "l7:Name";
    public static final String VALUE = "l7:Value";
    public static final String PROPERTIES = "l7:Properties";
    public static final String PROPERTY = "l7:Property";
    public static final String INT_VALUE = "l7:IntValue";
    public static final String LONG_VALUE = "l7:LongValue";
    public static final String BOOLEAN_VALUE = "l7:BooleanValue";
    public static final String STRING_VALUE = "l7:StringValue";
    public static final String RESOURCE = "l7:Resource";
    public static final String RESOURCES = "l7:Resources";
    public static final String RESOURCE_SET = "l7:ResourceSet";
    public static final String ID = "l7:Id";
    public static final String TYPE = "l7:Type";
    public static final String ITEM = "l7:Item";
    public static final String DEPENDENCIES = "l7:Dependencies";
    public static final String DEPENDENCY_GRAPH = "l7:DependencyGraph";
    public static final String GUID = "l7:Guid";
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_FOLDER_ID = "folderId";

    // Service
    public static final String SERVICE = "l7:Service";
    public static final String SERVICE_DETAIL = "l7:ServiceDetail";

    // Policy
    public static final String POLICY = "l7:Policy";
    public static final String ATTRIBUTE_GUID = "guid";
    public static final String POLICY_DETAIL = "l7:PolicyDetail";
    public static final String POLICY_TYPE = "l7:PolicyType";

    // Policy Backed Services
    public static final String POLICY_BACKED_SERVICE = "l7:PolicyBackedService";
    public static final String INTERFACE_NAME = "l7:InterfaceName";
    public static final String POLICY_BACKED_SERVICE_OPERATIONS = "l7:PolicyBackedServiceOperations";
    public static final String POLICY_BACKED_SERVICE_OPERATION = "l7:PolicyBackedServiceOperation";
    public static final String POLICY_ID = "l7:PolicyId";
    public static final String OPERATION_NAME = "l7:OperationName";

    // Folder
    public static final String FOLDER = "l7:Folder";

    // Encass
    public static final String ENCAPSULATED_ASSERTION = "l7:EncapsulatedAssertion";
    public static final String POLICY_REFERENCE = "l7:PolicyReference";
    public static final String ENCAPSULATED_RESULTS = "l7:EncapsulatedResults";
    public static final String ENCAPSULATED_ASSERTION_RESULT = "l7:EncapsulatedAssertionResult";
    public static final String RESULT_NAME = "l7:ResultName";
    public static final String RESULT_TYPE = "l7:ResultType";
    public static final String ENCAPSULATED_ARGUMENTS = "l7:EncapsulatedArguments";
    public static final String ENCAPSULATED_ASSERTION_ARGUMENT = "l7:EncapsulatedAssertionArgument";
    public static final String ARGUMENT_NAME = "l7:ArgumentName";
    public static final String ARGUMENT_TYPE = "l7:ArgumentType";

    // Cluster Property
    public static final String CLUSTER_PROPERTY = "l7:ClusterProperty";

    // Listen Port Elements
    public static final String LISTEN_PORT = "l7:ListenPort";
    public static final String PROTOCOL = "l7:Protocol";
    public static final String PORT = "l7:Port";
    public static final String ENABLED_FEATURES = "l7:EnabledFeatures";
    public static final String TARGET_SERVICE_REFERENCE = "l7:TargetServiceReference";
    public static final String TLS_SETTINGS = "l7:TlsSettings";
    public static final String CLIENT_AUTHENTICATION = "l7:ClientAuthentication";
    public static final String ENABLED_VERSIONS = "l7:EnabledVersions";
    public static final String ENABLED_CIPHER_SUITES = "l7:EnabledCipherSuites";

    private BundleElementNames() {
        //
    }
}
