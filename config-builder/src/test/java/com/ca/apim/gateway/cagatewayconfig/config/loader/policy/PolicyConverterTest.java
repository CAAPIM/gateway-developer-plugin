/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;

class PolicyConverterTest {

    @Test
    void removeExtension() {
        String extension = ".ex.ten.sion";
        PolicyConverter policyConverter = new TestPolicyConverter(extension);

        File policy = new File("policy.ex.ten.sion");
        Assert.assertEquals("policy", policyConverter.removeExtension(policy.getName()));

        policy = new File(File.separator + " some" + File.separator + " path" + File.separator + " my.policy.ex.ten.sion");
        Assert.assertEquals(File.separator + " some" + File.separator + " path" + File.separator + " my.policy", policyConverter.removeExtension(policy.getPath()));

        policy = new File(File.separator + " some" + File.separator + " path" + File.separator + " my.policy.ex.other.sion");
        Assert.assertEquals(File.separator + " some" + File.separator + " path" + File.separator + " my.policy.ex.other.sion", policyConverter.removeExtension(policy.getPath()));

        policy = new File(".ex.ten.sion");
        Assert.assertEquals("", policyConverter.removeExtension(policy.getName()));

        policy = new File(".sion");
        Assert.assertEquals(".sion", policyConverter.removeExtension(policy.getName()));
    }

    private static class TestPolicyConverter implements PolicyConverter {
        private final String extension;

        public TestPolicyConverter() {
            extension = ".xml";
        }

        public TestPolicyConverter(String extension) {
            this.extension = extension;
        }

        @Override
        public String getPolicyTypeExtension() {
            return extension;
        }

        @Override
        public String getPolicyXML(Policy policy, String policyString) {
            return "converted!" + policyString;
        }

        @Override
        public boolean canConvert(String name, Element policy) {
            return false;
        }

        @Override
        public InputStream convertFromPolicyElement(Element policy) {
            return null;
        }
    }
}