/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.GatewayEntity;

public interface EntityLinker<E extends GatewayEntity> extends EntitiesLinker {

    default void link(Bundle filteredBundle, Bundle bundle) {
        filteredBundle.getEntities(getEntityClass()).values().forEach(e -> link(e, bundle, filteredBundle));
    }

    Class<E> getEntityClass();

    void link(E encass, Bundle bundle, Bundle targetBundle);

}
