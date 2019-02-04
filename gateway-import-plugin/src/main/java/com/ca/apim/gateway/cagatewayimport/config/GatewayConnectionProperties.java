/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayimport.config;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public class GatewayConnectionProperties {
    private Property<String> url;
    private Property<String> userName;
    private Property<String> userPass;

    public GatewayConnectionProperties(Project project) {
        url = project.getObjects().property(String.class);
        userName = project.getObjects().property(String.class);
        userPass = project.getObjects().property(String.class);
    }

    /**
     * The restman url that the gateway can be reached at. For example: https://localhost:8443/restman
     *
     * @return The url for restman
     */
    @Input
    public Property<String> getUrl() {
        return url;
    }

    /**
     * The username to access restman using. Must have the Administrator role
     *
     * @return the username to used to authenticate with when exporting from the gateway
     */
    @Input
    public Property<String> getUserName() {
        return userName;
    }

    /**
     * The user password to access restman using.
     *
     * @return the password to used to authenticate with when exporting from the gateway
     */
    @Input
    public Property<String> getUserPass() {
        return userPass;
    }

}
