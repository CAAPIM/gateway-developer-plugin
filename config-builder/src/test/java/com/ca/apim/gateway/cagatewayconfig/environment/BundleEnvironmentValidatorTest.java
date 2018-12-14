/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.APPLICATION;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundleEnvironmentValidatorTest {

    @Test
    void validateEnvironmentProvidedEmptyBundle() {
        Bundle environmentBundle = new Bundle();
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        bundleEnvironmentValidator.validateEnvironmentProvided("emptyBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

    }

    @Test
    void validateEnvironmentProvidedPassword() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getStoredPasswords().put("myPassword", new StoredPassword());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"SECURE_PASSWORD\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myPassword</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingPassword() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getStoredPasswords().put("someOtherPassword", new StoredPassword());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"SECURE_PASSWORD\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myPassword</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myPassword"));
    }

    @Test
    void validateEnvironmentProvidedJDBCConnection() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getJdbcConnections().put("myConnection", new JdbcConnection());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"JDBC_CONNECTION\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myConnection</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingJDBCConnection() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getStoredPasswords().put("someOtherConnection", new StoredPassword());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"JDBC_CONNECTION\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myConnection</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myConnection"));
    }

    @Test
    void validateEnvironmentProvidedIDP() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getIdentityProviders().put("myIDP", new IdentityProvider());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"ID_PROVIDER_CONFIG\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myIDP</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingIDP() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getIdentityProviders().put("someOtherIDP", new IdentityProvider());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"ID_PROVIDER_CONFIG\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myIDP</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myIDP"));
    }

    @Test
    void validateEnvironmentProvidedClusterProperty() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getEnvironmentProperties().put(PREFIX_GATEWAY + "myProperty", new EnvironmentProperty("myOtherProperty", "value", Type.GLOBAL));
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"CLUSTER_PROPERTY\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myProperty</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingClusterProperty() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getEnvironmentProperties().put("myOtherProperty", new EnvironmentProperty("myOtherProperty", "value", Type.LOCAL));
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"CLUSTER_PROPERTY\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myProperty</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myProperty"));
    }

    @Test
    void validateEnvironmentProvidedTrustedCert() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getTrustedCerts().put("myCert", new TrustedCert());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"TRUSTED_CERT\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myCert</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingTrustedCert() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getTrustedCerts().put("myOtherCert", new TrustedCert());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"TRUSTED_CERT\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myCert</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myCert"));
    }

    @Test
    void validateEnvironmentProvidedMissingTrustedCertOnPluginMode() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getTrustedCerts().put("myOtherCert", new TrustedCert());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"TRUSTED_CERT\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>myCert</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", PLUGIN);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myCert"));
    }

    @Test
    void validateEnvironmentProvidedMissingPrivateKey() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getPrivateKeys().put("test1", new PrivateKey());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"00000000000000000000000000000005:test\" type=\"SSG_KEY_ENTRY\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>test</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("test"));
    }

    @Test
    void validateEnvironmentProvidedMissingPrivateKeyNoErrorOnPluginMode() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getPrivateKeys().put("test1", new PrivateKey());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"00000000000000000000000000000005:test\" type=\"SSG_KEY_ENTRY\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>test</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", PLUGIN);
    }

    @Test
    void validateEnvironmentProvidedCassandra() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getCassandraConnections().put("cassandra", new CassandraConnection());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);

        bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"CASSANDRA_CONFIGURATION\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>cassandra</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);
    }

    @Test
    void validateEnvironmentProvidedMissingCassandra() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getCassandraConnections().put("cassandra1", new CassandraConnection());
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"CASSANDRA_CONFIGURATION\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>cassandra</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("cassandra"));
    }

    @Test
    void validateEnvironmentProvidedMissingUnknownEntity() {
        Bundle environmentBundle = new Bundle();
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"GENERIC\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>name</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>entity</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("entity"));
    }

    @Test
    void validateWithWrongMappingType() {
        Bundle environmentBundle = new Bundle();
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        Executable validateBundle = () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "" +
                "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>" +
                "    </l7:References>\n" +
                "    <l7:Mappings>" +
                "        <l7:Mapping action=\"NewOrExisting\" srcId=\"89dbda0631bd25a08c73c96aebec7f5a\" type=\"CASSANDRA_CONFIGURATION\">\n" +
                "            <l7:Properties>\n" +
                "                <l7:Property key=\"MapBy\">\n" +
                "                    <l7:StringValue>id</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"MapTo\">\n" +
                "                    <l7:StringValue>entity</l7:StringValue>\n" +
                "                </l7:Property>\n" +
                "                <l7:Property key=\"FailOnNew\">\n" +
                "                    <l7:BooleanValue>true</l7:BooleanValue>\n" +
                "                </l7:Property>\n" +
                "            </l7:Properties>\n" +
                "        </l7:Mapping>" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", APPLICATION);

        assertThrows(DeploymentBundleException.class, validateBundle);
    }

    @Test
    void validateWithInvalidXML() {
        Bundle environmentBundle = new Bundle();
        BundleEnvironmentValidator bundleEnvironmentValidator = new BundleEnvironmentValidator(environmentBundle);
        assertThrows(DeploymentBundleException.class, () -> bundleEnvironmentValidator.validateEnvironmentProvided("myBundle", "bundle", APPLICATION));
    }
}