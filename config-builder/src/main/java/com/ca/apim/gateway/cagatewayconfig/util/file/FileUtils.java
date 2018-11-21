/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    public static final boolean POSIX_ENABLED = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
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

    private byte[] readFile(final File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new EntityBuilderException("Could not read file " + file.getPath(), e);
        }
    }

    /**
     * Close a {@link java.io.Closeable} without throwing any exceptions.
     *
     * @param closeable the object to close.
     */
    public static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "IO error when closing closeable '" + ioe.getMessage() + "'");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unexpected error when closing object", e);
            }
        }
    }
}
