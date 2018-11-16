/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Singleton
public class PolicyConverterRegistry {
    private final Set<PolicyConverter> policyConverters;

    @Inject
    public PolicyConverterRegistry(final Set<PolicyConverter> converters) {
        this.policyConverters = unmodifiableSet(converters);
    }

    @NotNull
    public PolicyConverter getConverterFromFileName(String fileName) {
        return policyConverters.stream()
                .filter(converter -> fileName.endsWith(converter.getPolicyTypeExtension()))
                .findFirst().orElseThrow(() -> new PolicyConverterException("Unknown policy file extension for file: " + fileName));
    }

    @NotNull
    public PolicyConverter getFromPolicyElement(String name, Element policy) {
        return policyConverters.stream()
                .filter(converter -> converter.canConvert(name, policy))
                .findFirst().orElseGet(() -> getConverterFromFileName(XMLPolicyConverter.EXTENSION));
    }
}
