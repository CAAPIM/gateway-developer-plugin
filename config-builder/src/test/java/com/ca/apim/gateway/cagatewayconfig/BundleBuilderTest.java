/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleBuilderTest {

    @Mock
    DocumentFileUtils documentFileUtils;
    @Mock
    EntityLoaderRegistry entityLoaderRegistry;
    @Mock
    BundleEntityBuilder bundleEntityBuilder;
    @Mock
    EntityBundleLoader entityBundleLoader;
    @Mock
    DocumentTools documentTools;
    @Mock
    DocumentBuilder documentBuilder;

    @BeforeEach
    void beforeEach() {
        when(documentTools.getDocumentBuilder()).thenReturn(documentBuilder);
    }

    @Test
    void buildBundleNoSource() {
        BundleBuilder bundleBuilder = new BundleBuilder(documentTools, documentFileUtils, entityLoaderRegistry, bundleEntityBuilder, entityBundleLoader);
        bundleBuilder.buildBundle(null, new File("output"), Collections.emptySet(), "my-bundle");

        verify(bundleEntityBuilder).build(argThat(bundle -> bundle.getPolicies().isEmpty()), eq(EntityBuilder.BundleType.DEPLOYMENT), any());
    }

    @Test
    void buildBundleOnePolicy() {
        Policy policy = new Policy();
        policy.setName("from-file");
        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestPolicyLoader(policy)));

        BundleBuilder bundleBuilder = new BundleBuilder(documentTools, documentFileUtils, entityLoaderRegistry, bundleEntityBuilder, entityBundleLoader);
        bundleBuilder.buildBundle(new File("input"), new File("output"), Collections.emptySet(), "my-bundle");

        verify(bundleEntityBuilder).build(argThat(bundle -> bundle.getPolicies().containsKey(policy.getName()) && bundle.getPolicies().containsValue(policy)), eq(EntityBuilder.BundleType.DEPLOYMENT), any());
    }

    static class TestPolicyLoader implements EntityLoader {
        private final Policy policy;

        public TestPolicyLoader() {
            policy = new Policy();
        }

        TestPolicyLoader(Policy policy) {
            this.policy = policy;
        }

        @Override
        public void load(Bundle bundle, File rootDir) {
            load(bundle, policy.getName(), "value");
        }

        @Override
        public void load(Bundle bundle, String name, String value) {
            bundle.getPolicies().put(name, policy);
        }

        @Override
        public String getEntityType() {
            return "POLICY_TEST";
        }
    }
}