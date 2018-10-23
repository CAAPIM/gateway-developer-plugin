/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
                "</l7:Bundle>");

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
                "</l7:Bundle>");
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
                "</l7:Bundle>");

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
                "</l7:Bundle>");
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
                "</l7:Bundle>");

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
                "</l7:Bundle>");
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
                "</l7:Bundle>");

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myIDP"));
    }

    @Test
    void validateEnvironmentProvidedClusterProperty() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getEnvironmentProperties().put(PREFIX_GATEWAY + "myProperty", "value");
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
                "</l7:Bundle>");
    }

    @Test
    void validateEnvironmentProvidedMissingClusterProperty() {
        Bundle environmentBundle = new Bundle();
        environmentBundle.getEnvironmentProperties().put("myOtherProperty", "value");
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
                "</l7:Bundle>");

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
                "</l7:Bundle>");
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
                "</l7:Bundle>");

        MissingEnvironmentException exception = assertThrows(MissingEnvironmentException.class, validateBundle);
        assertTrue(exception.getMessage().contains("myCert"));
    }
}