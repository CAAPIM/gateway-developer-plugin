/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import org.apache.commons.lang3.StringUtils;

public class ProjectInfo {
    private final String name;
    private final String groupName;
    private final String version;

    public ProjectInfo(String name, String groupName, String version) {
        this.name = name;
        this.groupName = groupName;
        this.version = StringUtils.equalsIgnoreCase(version, "unspecified") ? "" : version;
    }

    public String getName() {
        return name;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getVersion() {
        return version;
    }
}
