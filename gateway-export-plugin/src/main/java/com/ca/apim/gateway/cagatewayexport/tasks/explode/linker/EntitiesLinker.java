/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;

import java.io.File;
import java.util.Set;

public interface EntitiesLinker {

    void link(Bundle filteredBundle, Bundle bundle);

    default void link(Bundle filteredBundle, Bundle bundle, File rootFolder) {
        link(filteredBundle, bundle);
    }

    /**
     *  It has to be overridden in entity specific likers to change the dependency name or type
     * @param dependencies
     */
    default void link(Set<Dependency> dependencies) {
    }
}
