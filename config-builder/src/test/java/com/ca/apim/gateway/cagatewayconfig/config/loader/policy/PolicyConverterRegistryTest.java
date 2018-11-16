/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import org.w3c.dom.Element;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyConverterRegistryTest {

    @Test
    void getConverterFromFileName() {
        PolicyConverterTest textExtConverter = new PolicyConverterTest(".test.ext", "test");
        PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(textExtConverter));

        assertEquals(textExtConverter, policyConverterRegistry.getConverterFromFileName("hello.test.ext"));
        assertThrows(PolicyConverterException.class, () -> policyConverterRegistry.getConverterFromFileName("hello.test"));
    }

    @Test
    void getFromPolicyElement() {
        PolicyConverterTest textExtConverter = new PolicyConverterTest(".test.ext", "hello");
        PolicyConverterTest defaultConverter = new PolicyConverterTest(".xml", "default-converter");
        PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(textExtConverter, defaultConverter));

        assertEquals(textExtConverter, policyConverterRegistry.getFromPolicyElement("hello", null));
        assertEquals(defaultConverter, policyConverterRegistry.getFromPolicyElement("another-name", null));
    }


    static class PolicyConverterTest implements PolicyConverter {

        private String name;
        private String extension;

        public PolicyConverterTest() {
        }

        public PolicyConverterTest(String extension, String name) {
            this.extension = extension;
            this.name = name;
        }

        @Override
        public String getPolicyTypeExtension() {
            return extension;
        }

        @Override
        public String getPolicyXML(Policy policy, String policyString) {
            return null;
        }

        @Override
        public boolean canConvert(String name, Element policy) {
            return StringUtils.equals(name, this.name);
        }

        @Override
        public InputStream convertFromPolicyElement(Element policy) {
            return null;
        }
    }
}