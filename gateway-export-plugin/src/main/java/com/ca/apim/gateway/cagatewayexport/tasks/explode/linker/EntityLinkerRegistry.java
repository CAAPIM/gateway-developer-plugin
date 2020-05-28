/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class EntityLinkerRegistry {

    private final Collection<EntitiesLinker> entityLinkers;

    @Inject
    public EntityLinkerRegistry(final Set<EntitiesLinker> linkers) {
        this.entityLinkers = unmodifiableCollection(sortLinkers(linkers));
    }

    public Collection<EntitiesLinker> getEntityLinkers() {
        return entityLinkers;
    }

    private static final Map<Class<?>, EntitiesLinker> LINKERS_ORDER = new LinkedHashMap<>();

    static {
        LINKERS_ORDER.put(EncassLinker.class, null);
        LINKERS_ORDER.put(ServiceLinker.class, null);
    }

    private Collection<EntitiesLinker> sortLinkers(final Collection<EntitiesLinker> linkers) {
        Set<EntitiesLinker> orderedLinkers = new LinkedHashSet<>(linkers);
        orderedLinkers.removeIf(linker -> {
            if (LINKERS_ORDER.containsKey(linker.getClass())) {
                LINKERS_ORDER.put(linker.getClass(), linker);
                return true;
            }
            return false;
        });
        for (Map.Entry<Class<?>, EntitiesLinker> classEntitiesLinkerEntry : LINKERS_ORDER.entrySet()) {
            orderedLinkers.add(classEntitiesLinkerEntry.getValue());
        }
        return orderedLinkers;
    }
}
