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
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleFileBuilderTest {

    @Mock
    DocumentFileUtils documentFileUtils;
    @Mock
    EntityLoaderRegistry entityLoaderRegistry;
    @Mock
    BundleEntityBuilder bundleEntityBuilder;
    @Mock
    DocumentTools documentTools;
    @Mock
    DocumentBuilder documentBuilder;
    @Mock
    BundleCache bundleCache;

    @BeforeEach
    void beforeEach() {
        when(documentTools.getDocumentBuilder()).thenReturn(documentBuilder);
    }

    @Test
    void buildBundleNoSource() {
        BundleFileBuilder bundleFileBuilder = new BundleFileBuilder(documentTools, documentFileUtils, entityLoaderRegistry, bundleEntityBuilder, bundleCache);
        bundleFileBuilder.buildBundle(null, new File("output"), Collections.emptyList(), "my-bundle", "1.0");

        verify(bundleEntityBuilder).build(argThat(bundle -> bundle.getPolicies().isEmpty()),
                eq(EntityBuilder.BundleType.DEPLOYMENT), any(), eq("my-bundle"), eq("1.0"));
    }

    @Test
    void buildBundleOnePolicy() {
        Policy policy = new Policy();
        policy.setName("from-file");
        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestPolicyLoader(policy)));

        BundleFileBuilder bundleFileBuilder = new BundleFileBuilder(documentTools, documentFileUtils, entityLoaderRegistry, bundleEntityBuilder, bundleCache);
        bundleFileBuilder.buildBundle(new File("input"), new File("output"), Collections.emptyList(), "my-bundle", "1.0");

        verify(bundleEntityBuilder).build(argThat(bundle -> bundle.getPolicies().containsKey(policy.getName()) && bundle.getPolicies().containsValue(policy)), eq(EntityBuilder.BundleType.DEPLOYMENT), any(), eq("my-bundle"), eq("1.0"));
    }

    @Test
    void buildBundleWithDependency() {
        Policy policy = new Policy();
        policy.setName("from-file");
        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestPolicyLoader(policy)));

        List<File> dummyList = new ArrayList<>();
        dummyList.add(new File("test"));
        when(bundleCache.getBundleFromFile(any(File.class))).thenReturn(new Bundle());

        BundleFileBuilder bundleFileBuilder = Mockito.spy(new BundleFileBuilder(documentTools, documentFileUtils, entityLoaderRegistry, bundleEntityBuilder, bundleCache));
        bundleFileBuilder.buildBundle(new File("input"), new File("output"), dummyList, "my-bundle", "1.0");

        verify(bundleFileBuilder, Mockito.times(2)).logOverriddenEntities(any(Bundle.class), any(), any());
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
        public Object loadSingle(String name, File entitiesFile) {
            return null;
        }

        @Override
        public String getEntityType() {
            return "POLICY_TEST";
        }
    }
}