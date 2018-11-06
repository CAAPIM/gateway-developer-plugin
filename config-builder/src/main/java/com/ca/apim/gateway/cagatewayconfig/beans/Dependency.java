/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import java.util.Objects;

public class Dependency {
    private final String id;
    private final Class<? extends GatewayEntity> type;

    public Dependency(String id, Class<? extends GatewayEntity> type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Class<? extends GatewayEntity> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type);
    }
}
