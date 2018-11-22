/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.entity;

/**
 * Constants for Gateway entity types supported by the plugin.
 */
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class EntityTypes {

    public static final String CLUSTER_PROPERTY_TYPE = "CLUSTER_PROPERTY";
    public static final String LISTEN_PORT_TYPE = "SSG_CONNECTOR";
    public static final String ENCAPSULATED_ASSERTION_TYPE = "ENCAPSULATED_ASSERTION";
    public static final String FOLDER_TYPE = "FOLDER";
    public static final String POLICY_BACKED_SERVICE_TYPE = "POLICY_BACKED_SERVICE";
    public static final String SERVICE_TYPE = "SERVICE";
    public static final String POLICY_TYPE = "POLICY";
    public static final String ID_PROVIDER_CONFIG_TYPE = "ID_PROVIDER_CONFIG";
    public static final String STORED_PASSWORD_TYPE = "SECURE_PASSWORD";
    public static final String JDBC_CONNECTION = "JDBC_CONNECTION";
    public static final String TRUSTED_CERT_TYPE = "TRUSTED_CERT";
    public static final String PRIVATE_KEY_TYPE = "SSG_KEY_ENTRY";
    public static final String CASSANDRA_CONNECTION_TYPE = "CASSANDRA_CONFIGURATION";
    public static final String SCHEDULED_TASK_TYPE = "SCHEDULED_TASK";
    public static final String JMS_DESTINATION_TYPE = "JMS_ENDPOINT";

    private EntityTypes() { }
}
