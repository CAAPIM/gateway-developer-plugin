package com.ca.apim.gateway.cagatewayexport.util.string;

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

    public static boolean containsInvalidCharacter(String name) {
        return name.contains("_¯") || name.contains("¯_") || name.contains("\\") || name.contains("/");
    }
}
