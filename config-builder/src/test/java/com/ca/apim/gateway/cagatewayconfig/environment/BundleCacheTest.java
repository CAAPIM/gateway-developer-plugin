package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleCacheTest {
    @Mock
    EntityBundleLoader entityBundleLoader;
    final static String TEST_STRING = "test";

    @Test
    void getBundleFromFile() {
        File file = new File(TEST_STRING);
        Policy policy = new Policy();
        policy.setName(TEST_STRING);
        policy.setPath(TEST_STRING);
        Bundle bundle = new Bundle();
        bundle.getPolicies().put(policy.getPath(), policy);

        when(entityBundleLoader.load(file)).thenReturn(bundle);

        BundleCache cache = new BundleCache(entityBundleLoader);
        cache.getBundleFromFile(file);

        Assert.assertTrue(cache.contains(file.getPath()));
    }
}
