/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.EntityConfigException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public enum PolicyType {
    INCLUDE("Include", Policy.class),
    SERVICE_OPERATION("Service Operation", Policy.class),
    GLOBAL("Global", GlobalPolicy.class),
    INTERNAL("Internal", AuditPolicy.class,
            "audit-lookup",
            "audit-sink",
            "audit-message-filter",
            "audit-viewer");

    private String type;
    private List<String> tags;
    private Class<? extends Policy> policyClass;

    PolicyType(String type, Class<? extends Policy> policyClass, String... tags) {
        this.type = type;
        this.policyClass = policyClass;
        this.tags = new ArrayList<>(asList(firstNonNull(tags, new String[0])));
    }

    public String getType() {
        return type;
    }

    public Policy createPolicyObject() {
        try {
            return policyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new EntityConfigException("Could not create policy object for " + policyClass.getSimpleName(), e);
        }
    }

    public static boolean isValidType(String type, String tag) {
        // check if type is accepted
        PolicyType policyType = stream(values()).filter(t -> t.type.equals(type)).findFirst().orElse(null);
        if (policyType == null) {
            return false;
        }

        // check tags if specified, type is accepted if no tag restriction
        return isEmpty(tag) || policyType.tags.isEmpty() || policyType.tags.contains(tag);
    }

    public static PolicyType fromType(String policyType) {
        return stream(values()).filter(t -> t.type.equals(policyType)).findFirst().orElse(null);
    }
}
