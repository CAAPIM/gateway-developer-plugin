/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang.StringUtils.isEmpty;

public enum PolicyType {
    INCLUDE("Include"),
    SERVICE_OPERATION("Service Operation"),
    GLOBAL("Global"),
    INTERNAL("Internal",
            "audit-lookup",
            "audit-sink",
            "audit-message-filter",
            "audit-viewer");

    private String type;
    private List<String> tags;

    PolicyType(String type, String... tags) {
        this.type = type;
        this.tags = new ArrayList<>(asList(firstNonNull(tags, new String[0])));
    }

    public String getType() {
        return type;
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
