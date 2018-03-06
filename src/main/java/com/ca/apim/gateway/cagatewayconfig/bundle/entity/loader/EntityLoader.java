/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import org.w3c.dom.Element;

public interface EntityLoader<E extends Entity> {
    E load(final Element element);
}
