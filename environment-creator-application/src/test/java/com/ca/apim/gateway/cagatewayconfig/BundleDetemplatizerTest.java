/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.ContextVariableEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.ServiceEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty.Type;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BundleDetemplatizerTest {

    private String bundleXml = "<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
            "    <l7:References>\n" +
            "        <l7:Item>\n" +
            "            <l7:Name>my-gateway-api</l7:Name>\n" +
            "            <l7:Id>8263a394a3782fa4984bcffc2363b8db</l7:Id>\n" +
            "            <l7:Type>SERVICE</l7:Type>\n" +
            "            <l7:Resource>\n" +
            "                <l7:Service id=\"8263a394a3782fa4984bcffc2363b8db\">\n" +
            "                    <l7:ServiceDetail folderId=\"0000000000000000ffffffffffffec76\" id=\"8263a394a3782fa4984bcffc2363b8db\">\n" +
            "                        <l7:Name>my-gateway-api</l7:Name>\n" +
            "                        <l7:Enabled>true</l7:Enabled>\n" +
            "                        <l7:ServiceMappings>\n" +
            "                            <l7:HttpMapping>\n" +
            "                                <l7:UrlPattern>/example</l7:UrlPattern>\n" +
            "                                <l7:Verbs>\n" +
            "                                    <l7:Verb>DELETE</l7:Verb>\n" +
            "                                    <l7:Verb>POST</l7:Verb>\n" +
            "                                    <l7:Verb>GET</l7:Verb>\n" +
            "                                    <l7:Verb>PUT</l7:Verb>\n" +
            "                                </l7:Verbs>\n" +
            "                            </l7:HttpMapping>\n" +
            "                        </l7:ServiceMappings>\n" +
            "                        <l7:Properties>\n" +
            "                            <l7:Property key=\"property.ENV.myEnvironmentVariable\">\n" +
            "                                <l7:StringValue>SERVICE_PROPERTY_ENV.my-gateway-api.myEnvironmentVariable</l7:StringValue>\n" +
            "                            </l7:Property>\n" +
            "                        </l7:Properties>\n" +
            "                    </l7:ServiceDetail>\n" +
            "                    <l7:Resources>\n" +
            "                        <l7:ResourceSet tag=\"policy\">\n" +
            "                            <l7:Resource type=\"policy\">&lt;wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\"&gt;\n" +
            "    &lt;wsp:All wsp:Usage=\"Required\"&gt;\n" +
            "        &lt;L7p:SetVariable&gt;\n" +
            "            &lt;L7p:Base64Expression ENV_PARAM_NAME=\"ENV.anotherEnvVar\"/&gt;\n" +
            "            &lt;L7p:VariableToSet stringValue=\"ENV.anotherEnvVar\"/&gt;\n" +
            "        &lt;/L7p:SetVariable&gt;\n" +
            "        &lt;L7p:Authentication&gt;\n" +
            "            &lt;L7p:IdentityProviderName stringValue=\"Internal Identity Provider\"/&gt;\n" +
            "            &lt;L7p:Target target=\"RESPONSE\"/&gt;\n" +
            "        &lt;/L7p:Authentication&gt;\n" +
            "        &lt;L7p:Authentication&gt;\n" +
            "            &lt;L7p:IdentityProviderName stringValue=\"test-IDP\"/&gt;\n" +
            "            &lt;L7p:Target target=\"RESPONSE\"/&gt;\n" +
            "        &lt;/L7p:Authentication&gt;\n" +
            "    &lt;/wsp:All&gt;\n" +
            "&lt;/wsp:Policy&gt;\n" +
            "</l7:Resource>\n" +
            "                        </l7:ResourceSet>\n" +
            "                    </l7:Resources>\n" +
            "                </l7:Service>\n" +
            "            </l7:Resource>\n" +
            "        </l7:Item>\n" +
            "    </l7:References>\n" +
            "    <l7:Mappings>\n" +
            "        <l7:Mapping action=\"NewOrUpdate\" srcId=\"8263a394a3782fa4984bcffc2363b8db\" type=\"SERVICE\"/>\n" +
            "    </l7:Mappings>\n" +
            "</l7:Bundle>";

    @Test
    void detemplatizeBundleString() {
        Map<String,ServiceEnvironmentProperty> serviceEnv = new HashMap<>();
        serviceEnv.put("my-gateway-api.myEnvironmentVariable", new ServiceEnvironmentProperty("my-gateway-api.myEnvironmentVariable","abc"));
        Map<String,ContextVariableEnvironmentProperty> contextEnv = new HashMap<>();
        contextEnv.put("anotherEnvVar", new ContextVariableEnvironmentProperty("my-gateway-api.anotherEnvVar", "qwe"));
        String testIdpGoid = "8263a394a3782fa4984bcffc2363b8cc";
        Bundle bundle = new Bundle();
        bundle.putAllServiceEnvironmentProperties(serviceEnv);
        bundle.putAllContextVariableEnvironmentProperties(contextEnv);
        bundle.putAllIdentityProviders(ImmutableMap.of("test-IDP", new IdentityProvider.Builder().id(testIdpGoid).build()));

        BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(bundle);

        String detemplatizedBundle = bundleDetemplatizer.detemplatizeBundleString(bundleXml).toString();

        assertEquals("<l7:Bundle xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\">\n" +
                "    <l7:References>\n" +
                "        <l7:Item>\n" +
                "            <l7:Name>my-gateway-api</l7:Name>\n" +
                "            <l7:Id>8263a394a3782fa4984bcffc2363b8db</l7:Id>\n" +
                "            <l7:Type>SERVICE</l7:Type>\n" +
                "            <l7:Resource>\n" +
                "                <l7:Service id=\"8263a394a3782fa4984bcffc2363b8db\">\n" +
                "                    <l7:ServiceDetail folderId=\"0000000000000000ffffffffffffec76\" id=\"8263a394a3782fa4984bcffc2363b8db\">\n" +
                "                        <l7:Name>my-gateway-api</l7:Name>\n" +
                "                        <l7:Enabled>true</l7:Enabled>\n" +
                "                        <l7:ServiceMappings>\n" +
                "                            <l7:HttpMapping>\n" +
                "                                <l7:UrlPattern>/example</l7:UrlPattern>\n" +
                "                                <l7:Verbs>\n" +
                "                                    <l7:Verb>DELETE</l7:Verb>\n" +
                "                                    <l7:Verb>POST</l7:Verb>\n" +
                "                                    <l7:Verb>GET</l7:Verb>\n" +
                "                                    <l7:Verb>PUT</l7:Verb>\n" +
                "                                </l7:Verbs>\n" +
                "                            </l7:HttpMapping>\n" +
                "                        </l7:ServiceMappings>\n" +
                "                        <l7:Properties>\n" +
                "                            <l7:Property key=\"property.ENV.myEnvironmentVariable\">\n" +
                "                                <l7:StringValue>abc</l7:StringValue>\n" +
                "                            </l7:Property>\n" +
                "                        </l7:Properties>\n" +
                "                    </l7:ServiceDetail>\n" +
                "                    <l7:Resources>\n" +
                "                        <l7:ResourceSet tag=\"policy\">\n" +
                "                            <l7:Resource type=\"policy\">&lt;wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\"&gt;\n" +
                "    &lt;wsp:All wsp:Usage=\"Required\"&gt;\n" +
                "        &lt;L7p:SetVariable&gt;\n" +
                "            &lt;L7p:Base64Expression stringValue=\"cXdl\"/&gt;\n" +
                "            &lt;L7p:VariableToSet stringValue=\"ENV.anotherEnvVar\"/&gt;\n" +
                "        &lt;/L7p:SetVariable&gt;\n" +
                "        &lt;L7p:Authentication&gt;\n" +
                "            &lt;L7p:IdentityProviderOid goidValue=\"" + INTERNAL_IDP_ID +"\"/&gt;\n" +
                "            &lt;L7p:Target target=\"RESPONSE\"/&gt;\n" +
                "        &lt;/L7p:Authentication&gt;\n" +
                "        &lt;L7p:Authentication&gt;\n" +
                "            &lt;L7p:IdentityProviderOid goidValue=\"" + testIdpGoid + "\"/&gt;\n" +
                "            &lt;L7p:Target target=\"RESPONSE\"/&gt;\n" +
                "        &lt;/L7p:Authentication&gt;\n" +
                "    &lt;/wsp:All&gt;\n" +
                "&lt;/wsp:Policy&gt;\n" +
                "</l7:Resource>\n" +
                "                        </l7:ResourceSet>\n" +
                "                    </l7:Resources>\n" +
                "                </l7:Service>\n" +
                "            </l7:Resource>\n" +
                "        </l7:Item>\n" +
                "    </l7:References>\n" +
                "    <l7:Mappings>\n" +
                "        <l7:Mapping action=\"NewOrUpdate\" srcId=\"8263a394a3782fa4984bcffc2363b8db\" type=\"SERVICE\"/>\n" +
                "    </l7:Mappings>\n" +
                "</l7:Bundle>", detemplatizedBundle);
    }

    @Test
    void detemplatizeBundleStringMissingEnv() {
        Map<String,ContextVariableEnvironmentProperty> env = new HashMap<>();
        env.put("myEnvironmentVariable", new ContextVariableEnvironmentProperty("myEnvironmentVariable","abc"));
        Bundle bundle = new Bundle();
        bundle.putAllContextVariableEnvironmentProperties(env);

        BundleDetemplatizer bundleDetemplatizer = new BundleDetemplatizer(bundle);
        assertThrows(BundleDetemplatizeException.class, () -> bundleDetemplatizer.detemplatizeBundleString(bundleXml));
    }
}
