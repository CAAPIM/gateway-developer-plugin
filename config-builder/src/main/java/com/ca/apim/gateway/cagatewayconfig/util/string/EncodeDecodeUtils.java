/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

public class EncodeDecodeUtils {

    private EncodeDecodeUtils() {
    }

    public static String encodePath(String pathToEncode) {
        if (pathToEncode.contains("_¯") || pathToEncode.contains("¯_")) {
            throw new IllegalArgumentException("Illegal characters in path. Cannot contain '_¯' or '¯_': " + pathToEncode);
        }
        pathToEncode = pathToEncode.replaceAll("/", "_¯");
        pathToEncode = pathToEncode.replaceAll("\\\\", "¯_");
        return pathToEncode;
    }

    public static String decodePath(String pathToDecode) {
        pathToDecode = pathToDecode.replaceAll("_¯", "/");
        pathToDecode = pathToDecode.replaceAll("¯_", "\\\\");
        return pathToDecode;
    }

    public static boolean containsInvalidCharacter(String name) {
        return name.contains("_¯") || name.contains("¯_") || name.contains("\\") || name.contains("/");
    }
}
