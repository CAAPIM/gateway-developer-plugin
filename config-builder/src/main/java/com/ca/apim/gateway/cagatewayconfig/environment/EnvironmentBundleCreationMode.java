/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;

/**
 * Modes for bundle creation. Defines where its coming from and may modify behaviours.
 */
public enum EnvironmentBundleCreationMode {

    APPLICATION {
        @Override
        boolean isRequired(String entityType) {
            return true;
        }
    },

    PLUGIN {
        @Override
        boolean isRequired(String entityType) {
            return !PRIVATE_KEY_TYPE.equals(entityType);
        }
    };

    abstract boolean isRequired(String entityType);
}
