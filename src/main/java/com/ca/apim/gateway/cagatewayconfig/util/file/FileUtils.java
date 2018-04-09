/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileUtils {
    public static final FileUtils INSTANCE = new FileUtils();

    public InputStream getInputStream(final File file) {
        final InputStream stream;
        try {
            stream = Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new EntityBuilderException("Could not read file " + file.getPath(), e);
        }
        return stream;
    }

    public String getFileAsString(final File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new EntityBuilderException("Could not read file " + file.getPath(), e);
        }
    }
}
