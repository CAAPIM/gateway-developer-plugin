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
    private String majorVersion;
    private String minorVersion;
    private final String configName;
    private String targetFolder;

    public ProjectInfo(String name, String groupName, String version) {
        this(name, groupName, version, null);
    }

    public ProjectInfo(String name, String groupName, String version, String configName) {
        this.name = name;
        this.groupName = groupName;
        this.version = StringUtils.equalsIgnoreCase(version, "unspecified") ? "" : version;
        this.configName = configName;
        if (StringUtils.isNotBlank(this.version)) {
            String[] versionParts = version.split(".");
            if (versionParts.length > 0) {
                majorVersion = versionParts[0];
                minorVersion = "0";
            } else {
                majorVersion = "";
                minorVersion = "";
            }
            if (versionParts.length > 1) {
                minorVersion = versionParts[1];
            }
        }
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

    public String getMajorVersion() {
        return majorVersion;
    }

    public String getMinorVersion() {
        return  minorVersion;
    }

    public String getConfigName() {
        return configName;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public ProjectInfo withTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
        return this;
    }
}
