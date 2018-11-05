/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import org.w3c.dom.Element;

public interface BundleDependencyLoader {

    void load(final Bundle bundle, final Element element);

    String getEntityType();
}
