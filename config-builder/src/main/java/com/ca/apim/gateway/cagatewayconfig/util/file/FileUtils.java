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
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;

public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    public static final boolean POSIX_ENABLED = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    public static final FileUtils INSTANCE = new FileUtils();
    public static final String BUNDLE_EXTENSION = ".bundle";

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

    /**
     * Collect all files on a specific directory with specified extension
     *
     * @param folder folder to collect files
     * @param extension extension to filter
     * @return list of files
     */
    public static List<File> collectFiles(final String folder, final String extension) {
        File templatizedFolder = new File(folder);
        File[] templatizedBundles = ofNullable(templatizedFolder.listFiles((dir, name) -> name.endsWith(extension))).orElse(new File[0]);
        return newArrayList(templatizedBundles);
    }

    /**
     * Find config file or directory into the base dir specified.
     *
     * @param baseDir base dir
     * @param fileOrDirName file to be found
     * @return file found or null
     */
    public static File findConfigFileOrDir(final File baseDir, final String fileOrDirName) {
        return Stream.of(new File(baseDir, fileOrDirName), new File(new File(baseDir, "config"), fileOrDirName)).filter(File::exists).findFirst().orElse(null);
    }

    /**
     * Create all folder in this path. Does not fail if any of them already exist.
     *
     * @param folderPath Path representing all folders that should be created.
     */
    public synchronized void createFolders(Path folderPath) {
        if (!folderPath.toFile().exists()) {
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new DocumentFileUtilsException("Exception creating folder(s): " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new DocumentFileUtilsException("Wanted to create folder but found a file: " + folderPath);
        }
    }

    /**
     * Create single folder in this path. Does not fail if already exist.
     *
     * @param folderPath Path representing all folders that should be created.
     */
    public synchronized void createFolder(Path folderPath) {
        if (!folderPath.toFile().exists()) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                throw new DocumentFileUtilsException("Exception creating folder: " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new DocumentFileUtilsException("Wanted to create folder but found a file: " + folderPath);
        }
    }
}
