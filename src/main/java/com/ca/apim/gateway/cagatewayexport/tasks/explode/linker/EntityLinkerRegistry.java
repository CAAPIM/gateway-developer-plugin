/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class EntityLinkerRegistry {

    private final Collection<EntitiesLinker> entityLinkers;

    public EntityLinkerRegistry(final DocumentTools documentTools) {
        final Collection<EntitiesLinker> linkersCollection = new HashSet<>();
        linkersCollection.add(new PolicyLinker(documentTools));
        linkersCollection.add(new ServiceLinker(documentTools));
        linkersCollection.add(new EncassLinker());
        linkersCollection.add(new PolicyBackedServiceLinker());
        linkersCollection.add(new ClusterPropertyLinker());
        linkersCollection.add(new ListenPortLinker());

        this.entityLinkers = Collections.unmodifiableCollection(linkersCollection);
    }

    public Collection<EntitiesLinker> getEntityLinkers() {
        return entityLinkers;
    }
}
