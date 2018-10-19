/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;

import java.io.File;

public interface EntityWriter {
    void write(Bundle bundle, File rootFolder);

    default void write(String folderPath, Bundle bundle, File rootFolder) {
        write(bundle, rootFolder);
    }

}
