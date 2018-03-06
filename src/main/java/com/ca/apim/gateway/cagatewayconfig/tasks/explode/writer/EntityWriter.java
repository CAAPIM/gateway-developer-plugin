/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;

import java.nio.file.Path;

public interface EntityWriter<E extends Entity> {
    void write(Path path, E entity);
}
