/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder;

import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import org.w3c.dom.Element;

import java.io.File;

public interface EntityBuilder<E extends Entity> {


    E build(String name, String id, Element entityElement, File folder, String parentFolderID);
}
