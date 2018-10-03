/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.util.List;

public interface EntityBuilder extends Comparable<EntityBuilder> {

    List<Entity> build(Bundle bundle, Document document);

    /**
     * @return the position of the entity produced by this builder in the bundle file, has to be non null and not conflicting
     */
    @NotNull
    Integer getOrder();

    @Override
    default int compareTo(@NotNull EntityBuilder o) {
        return this.getOrder().compareTo(o.getOrder());
    }
}
