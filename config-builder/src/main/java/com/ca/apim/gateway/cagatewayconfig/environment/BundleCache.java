package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BundleCache {
    private final Map<String, Bundle> cache = new ConcurrentHashMap<>();

    @Inject
    public BundleCache() {
        // Wrapper class for a hashmap to be a persistent cache, no argument needed
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

}