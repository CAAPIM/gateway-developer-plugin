/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.paths;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

/**
 * Utility class to handle operations related to path names and separators.
 */
public class PathUtils {

    // Constant to be used for the path separation, forcing to be the unix separator.
    private static final String UNIX_PATH_SEPARATOR = "/";

    private PathUtils() {}

    public static String unixPath(@NotNull String first, String... more) {
        return separatorsToUnix(Paths.get(first, more).toString());
    }

    public static String unixPath(@NotNull Path path) {
        return separatorsToUnix(path.toString());
    }

    @NotNull
    public static String unixPathEndingWithSeparator(@NotNull String first, String... more) {
        return separatorsToUnix(Paths.get(first, more).toString()) + UNIX_PATH_SEPARATOR;
    }

    @NotNull
    public static String unixPathEndingWithSeparator(@NotNull Path path) {
        return separatorsToUnix(path.toString()) + UNIX_PATH_SEPARATOR;
    }

    public static String extractName(String nameWithPath) {
        int index = nameWithPath.lastIndexOf(UNIX_PATH_SEPARATOR);
        return index > -1 ? nameWithPath.substring(index + 1) : nameWithPath;
    }

    public static String extractPath(String nameWithPath) {
        int index = nameWithPath.lastIndexOf(UNIX_PATH_SEPARATOR);
        return index > -1 ? nameWithPath.substring(0, index + 1) : "";
    }
}
