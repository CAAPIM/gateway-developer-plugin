/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;

import java.util.List;

public interface EntityBuilder {
    List<Entity> build(Bundle bundle);
}
