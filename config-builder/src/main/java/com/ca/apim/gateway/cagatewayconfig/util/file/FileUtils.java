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
import java.io.OutputStream;
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

    public OutputStream getOutputStream(final File file) {
        final OutputStream stream;
        try {
            stream = Files.newOutputStream(file.toPath());
        } catch (IOException e) {
            throw new EntityBuilderException("Could not write to file " + file.getPath(), e);
        }
        return stream;
    }

    public String getFileAsString(final File file) {
        return new String(this.readFile(file));
    }

    public byte[] readFile(final File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new EntityBuilderException("Could not read file " + file.getPath(), e);
        }
    }

    public void writeContent(byte[] content, final File file) {
        try {
            Files.write(file.toPath(), content);
        } catch (IOException e) {
            throw new EntityBuilderException("Could not write content to file " + file.getPath(), e);
        }
    }
}
