/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.environment.CharacterBlacklist.getCharBlacklist;

/**
 * Utility class for Character Blacklist. Purpose is to apply functions to the constants found in CharacterBlacklist
 */
public class CharacterBlacklistUtil {
    private static final Logger LOGGER = Logger.getLogger(CharacterBlacklistUtil.class.getName());
    private static final char REPLACEMENT_CHAR = '-';

    /**
     * Find blacklist characters and replace with value '-'
     *
     * @param string with invalid characters
     * @return a string stripped of all invalid characters and replaced with value '-'
     */
    public static String filterAndReplace(String string) {
        Set<Character> charBlacklist = getCharBlacklist();

        for (char c : string.toCharArray()) {
            if (charBlacklist.contains(c)) {
                string = string.replace(c, REPLACEMENT_CHAR);
            }
        }

        return compressString(string, REPLACEMENT_CHAR);
    }

    /**
     * Compress repeating characters in String
     *
     * @param string with repeating characters
     * @param replacementChar to replace in string
     * @return string with selected repeating character removed
     */
    private static String compressString(String string, char replacementChar) {
        StringBuilder processedString = new StringBuilder(string);
        int iterator = 0;

        while (iterator < processedString.length() - 1) {
            if (processedString.charAt(iterator) == replacementChar && processedString.charAt(iterator + 1) == replacementChar) {
                processedString.deleteCharAt(iterator);
            } else {
                iterator++;
            }
        }

        return processedString.toString();
    }

    /**
     *
     * @param name with invalid characters
     * @return boolean indicating whether a invalid character exist
     */
    public static boolean containsInvalidCharacter(String name) {
        Set<Character> charBlacklist = getCharBlacklist();

        for (char c : name.toCharArray()) {
            if (charBlacklist.contains(c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns URL encoded name (even asterisk is encoded)
     */
    public static String encodeName(String name) {
        try {
            String encodedName = URLEncoder.encode(name, "UTF-8");
            return encodedName.replaceAll("\\*", "%2A");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "unable to encode folder name " + name);
            throw new RuntimeException("Unable to encode name " + name, e);
        }
    }

    /**
     * Returns URL encoded path (even asterisk is encoded)
     */
    public static String encodePath(String path) {
        if (path.equals("/")) {
            return path;
        }
        final String[] folderNames = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();
        for (String folderName : folderNames) {
            pathBuilder.append(encodeName(folderName));
            pathBuilder.append("/");
        }
        if (!path.endsWith("/")) {
            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
        }
        return pathBuilder.toString();
    }

    /**
     * Returns URL decoded path
     */
    public static String decodePath(String path) {
        if (path.equals("/")) {
            return path;
        }
        final String[] folderNames = path.split("/");
        StringBuilder pathBuilder = new StringBuilder();
        for (String folderName : folderNames) {
            pathBuilder.append(decodeName(folderName));
            pathBuilder.append("/");
        }
        if (!path.endsWith("/")) {
            pathBuilder.deleteCharAt(pathBuilder.length() - 1);
        }
        return pathBuilder.toString();
    }

    /**
     * Returns URL decoded name
     */
    public static String decodeName(String name) {
        try {
            return URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "unable to decode folder name " + name);
            throw new RuntimeException("Unable to decode name " + name, e);
        }

    }

    private CharacterBlacklistUtil() {}
}
