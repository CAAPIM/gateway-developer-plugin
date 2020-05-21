/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public final class BuilderConstants {

    private BuilderConstants(){
    }

    public static final String STORED_PASSWORD_REF_FORMAT = "${secpass.%s.plaintext}";

    protected static final Set<String> NON_ENV_ENTITY_TYPES;

    public static final Predicate<Entity> FILTER_ENV_ENTITIES = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            return !NON_ENV_ENTITY_TYPES.contains(entity.getType());
        }
    };

    public static final Predicate<Entity> FILTER_NON_ENV_ENTITIES = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            return NON_ENV_ENTITY_TYPES.contains(entity.getType());
        }
    };

    static {
        NON_ENV_ENTITY_TYPES = new HashSet<>();
        NON_ENV_ENTITY_TYPES.add(EntityTypes.FOLDER_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.POLICY_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.SERVICE_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
    }
}
