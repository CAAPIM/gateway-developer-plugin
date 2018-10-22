/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class EntityWriterRegistry {

    private final Collection<EntityWriter> entityWriters;

    @Inject
    public EntityWriterRegistry(final Set<EntityWriter> writers) {
        this.entityWriters = unmodifiableCollection(writers);
    }

    public Collection<EntityWriter> getEntityWriters() {
        return entityWriters;
    }
}
