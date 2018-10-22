/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.properties;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.enumeration;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Internal class used to read the properties file. Takes advantage of the existing default mechanism of reading and parsing properties files,
 * but stores them in a ordered structure. This is done in order to keep original order from the properties file.
 */
public class OrderedProperties extends Properties {

    private LinkedHashMap<Object, Object> propertyMap = new LinkedHashMap<>();

    @Override
    public synchronized Object put(Object key, Object value) {
        return propertyMap.put(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return propertyMap.get(key);
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        propertyMap.forEach(action);
    }

    @Override
    public synchronized int hashCode() {
        return propertyMap.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public synchronized boolean equals(Object o) {
        return propertyMap.equals(o);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return enumeration(propertyMap.keySet());
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return propertyMap.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return (String) propertyMap.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return (String) propertyMap.getOrDefault(key, defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return keys();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return propertyMap.keySet().stream().collect(toMap(Object::toString, identity())).keySet();
    }

    @Override
    public synchronized int size() {
        return propertyMap.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return enumeration(propertyMap.values());
    }

    @Override
    public synchronized boolean contains(Object value) {
        return propertyMap.containsValue(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return propertyMap.containsValue(value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return propertyMap.containsKey(key);
    }

    @Override
    public synchronized Object remove(Object key) {
        return propertyMap.remove(key);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        propertyMap.putAll(t);
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return propertyMap.keySet();
    }

    @NotNull
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return propertyMap.entrySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return propertyMap.values();
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return propertyMap.getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        propertyMap.replaceAll(function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return propertyMap.putIfAbsent(key, value);
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return propertyMap.remove(key, value);
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return propertyMap.replace(key, oldValue, newValue);
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return propertyMap.replace(key, value);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return propertyMap.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return propertyMap.computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return propertyMap.compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return propertyMap.merge(key, value, remappingFunction);
    }
}

