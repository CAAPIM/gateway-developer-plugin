/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import org.w3c.dom.Element;

public interface Entity {
    String getType();

    String getId();

    Element getXml();

    String getName();
}
