/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class CAGatewayDeveloperBase implements Plugin<Project> {
    @Override
    public void apply(@NotNull final Project project) {
        // The base plugin is meant to define any capabilities.

        // Applying the base plugin will make all the BuildBundle Task available but you will need to configure it yourself
    }
}

