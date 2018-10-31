/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.google.common.base.Joiner;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public abstract class PolicyLoaderBase extends EntityLoaderBase<Policy> {

    @Inject
    PolicyLoaderBase(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<Policy> getBeanClass() {
        return Policy.class;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, Policy> entitiesMap) {
        // block loading of different policies with repeated tags
        Set<String> errors = new HashSet<>();
        entitiesMap.values()
                .stream()
                .collect(groupingBy(Policy::getTag, mapping(identity(), toList())))
                .forEach((key, value) -> {
                    if (value.size() > 1) {
                        errors.add(
                                String.format(
                                        "Found more then one %s policy with tag '%s': [%s]",
                                        this.getPolicyType().getType(),
                                        key,
                                        Joiner.on(", ").join(value.stream().map(Policy::getPath).collect(toList()))
                                )
                        );
                    }
                });
        if (!errors.isEmpty()) {
            throw new BundleLoadException(Joiner.on("\n").join(errors));
        }

        final Map<String, Policy> policiesByPath = entitiesMap.values().stream().collect(Collectors.toMap(Policy::getPath, policy -> {
            policy.setPolicyType(this.getPolicyType());
            return policy;
        }));
        bundle.putAllPolicies(policiesByPath);
    }

    abstract PolicyType getPolicyType();
}
