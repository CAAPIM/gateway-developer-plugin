/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.util.gateway;

/**
 * Utility class to hold element names used in gateway bundles.
 */
@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
public class BundleElementNames {

    // Attributes
    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_KEY = "key";
    public static final String ATTRIBUTE_RESOURCE_URI= "resourceUri";
    public static final String ATTRIBUTE_ACTION = "action";
    public static final String ATTRIBUTE_SRCID = "srcId";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_FOLDER_ID = "folderId";
    public static final String ATTRIBUTE_ALIAS = "alias";
    public static final String ATTRIBUTE_KEYSTORE_ID = "keystoreId";
    public static final String ATTRIBUTE_KEY_ALGORITHM = "keyAlgorithm";
    public static final String ATTRIBUTE_TAG = "tag";
    public static final String ATTRIBUTE_ROOT_URL = "rootUrl";
    public static final String ATTRIBUTE_SOURCE_URL = "sourceUrl";

    // Common Elements
    public static final String NAME = "l7:Name";
    public static final String PROPERTIES = "l7:Properties";
    public static final String PROPERTY = "l7:Property";
    public static final String INT_VALUE = "l7:IntValue";
    public static final String INTEGER_VALUE = "l7:IntegerValue";
    public static final String LONG_VALUE = "l7:LongValue";
    public static final String BOOLEAN_VALUE = "l7:BooleanValue";
    public static final String STRING_VALUE = "l7:StringValue";
    public static final String DATE_VALUE = "l7:DateValue";
    public static final String RESOURCE = "l7:Resource";
    public static final String REFERENCE = "l7:Reference";
    public static final String RESOURCES = "l7:Resources";
    public static final String RESOURCE_SET = "l7:ResourceSet";
    public static final String BUNDLE = "l7:Bundle";
    public static final String REFERENCES = "l7:References";
    public static final String MAPPINGS = "l7:Mappings";
    public static final String MAPPING = "l7:Mapping";
    public static final String ITEM = "l7:Item";
    public static final String DEPENDENCIES = "l7:Dependencies";
    public static final String DEPENDENCY_GRAPH = "l7:DependencyGraph";
    public static final String ID = "l7:Id";
    public static final String TYPE = "l7:Type";
    public static final String GUID = "l7:Guid";
    public static final String ENABLED = "l7:Enabled";

    // Service
    public static final String SERVICE = "l7:Service";
    public static final String SERVICE_DETAIL = "l7:ServiceDetail";
    public static final String SERVICE_MAPPINGS = "l7:ServiceMappings";
    public static final String HTTP_MAPPING = "l7:HttpMapping";
    public static final String URL_PATTERN = "l7:UrlPattern";
    public static final String VERBS = "l7:Verbs";
    public static final String VERB = "l7:Verb";

    // Policy
    public static final String POLICY = "l7:Policy";
    public static final String ATTRIBUTE_GUID = "guid";
    public static final String POLICY_DETAIL = "l7:PolicyDetail";
    public static final String POLICY_TYPE = "l7:PolicyType";

    // Cluster Property
    public static final String CLUSTER_PROPERTY = "l7:ClusterProperty";
    public static final String VALUE = "l7:Value";

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
    public static final String ORDINAL = "l7:Ordinal";
    public static final String GUI_PROMPT = "l7:GuiPrompt";

    // Policy Backed Services
    public static final String POLICY_BACKED_SERVICE = "l7:PolicyBackedService";
    public static final String INTERFACE_NAME = "l7:InterfaceName";
    public static final String POLICY_BACKED_SERVICE_OPERATIONS = "l7:PolicyBackedServiceOperations";
    public static final String POLICY_BACKED_SERVICE_OPERATION = "l7:PolicyBackedServiceOperation";
    public static final String POLICY_ID = "l7:PolicyId";
    public static final String OPERATION_NAME = "l7:OperationName";

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

    // Stored Passwords
    public static final String STORED_PASSWD = "l7:StoredPassword";
    public static final String PASSWORD = "l7:Password";

    // JDBC Connections
    public static final String JDBC_CONNECTION = "l7:JDBCConnection";
    public static final String DRIVER_CLASS = "l7:DriverClass";
    public static final String JDBC_URL = "l7:JdbcUrl";
    public static final String CONNECTION_PROPERTIES = "l7:ConnectionProperties";

    //Trusted Cert Elements
    public static final String TRUSTED_CERT = "l7:TrustedCertificate";
    public static final String CERT_DATA = "l7:CertificateData";
    public static final String ISSUER_NAME = "l7:IssuerName";
    public static final String SERIAL_NUMBER = "l7:SerialNumber";
    public static final String SUBJECT_NAME = "l7:SubjectName";
    public static final String ENCODED = "l7:Encoded";

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

    // Private Key
    public static final String PRIVATE_KEY = "l7:PrivateKey";
    public static final String CERTIFICATE_CHAIN = "l7:CertificateChain";

    // Cassandra Connection
    public static final String CASSANDRA_CONNECTION = "l7:CassandraConnection";
    public static final String KEYSPACE = "l7:Keyspace";
    public static final String CONTACT_POINT = "l7:ContactPoint";
    public static final String USERNAME = "l7:Username";
    public static final String PASSWORD_ID = "l7:PasswordId";
    public static final String COMPRESSION = "l7:Compression";
    public static final String SSL = "l7:Ssl";
    public static final String TLS_CIPHERS = "l7:TlsCiphers";

    // Scheduled Tasks
    public static final String SCHEDULED_TASK = "l7:ScheduledTask";
    public static final String ONE_NODE = "l7:OneNode";
    public static final String JOB_TYPE = "l7:JobType";
    public static final String JOB_STATUS = "l7:JobStatus";
    public static final String EXECUTION_DATE = "l7:ExecutionDate";
    public static final String CRON_EXPRESSION = "l7:CronExpression";
    public static final String EXECUTE_ON_CREATE = "l7:ExecuteOnCreate";

    // JMS Destination
    public static final String JMS_DESTINATION = "l7:JMSDestination";
    public static final String JMS_DESTINATION_DETAIL = "l7:JMSDestinationDetail";
    public static final String JMS_DESTINATION_NAME = "l7:DestinationName";
    public static final String INBOUND = "l7:Inbound";
    public static final String TEMPLATE = "l7:Template";
    public static final String JMS_CONNECTION = "l7:JMSConnection";
    public static final String JMS_PROVIDER_TYPE = "l7:ProviderType";
    public static final String CONTEXT_PROPERTIES_TEMPLATE = "l7:ContextPropertiesTemplate";
    
    private BundleElementNames() {
        //
    }
}
