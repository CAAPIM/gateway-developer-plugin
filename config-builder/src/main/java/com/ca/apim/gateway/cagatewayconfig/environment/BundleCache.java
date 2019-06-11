package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingOperation;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BundleCache {
    private final Map<String, Bundle> cache = new ConcurrentHashMap<>();
    private final EntityBundleLoader entityBundleLoader;

    @Inject
    public BundleCache(final EntityBundleLoader entityBundleLoader) {
        // Wrapper class for a hashmap to be a persistent cache, no argument needed
        this.entityBundleLoader = entityBundleLoader;
    }

    public Bundle getBundle(String bundlePath) {
        return cache.get(bundlePath);
    }

    public boolean contains(String bundlePath) {
        return cache.containsKey(bundlePath);
    }

    public void putBundle(String bundlePath, Bundle bundle) {
        cache.put(bundlePath, bundle);
    }

    public Bundle getBundleFromFile(File file) {
        if (!cache.containsValue(file.getPath())) {
            cache.put(file.getPath(), entityBundleLoader.load(file, BundleLoadingOperation.EXPORT));
        }
        return cache.get(file.getPath());
    }
}