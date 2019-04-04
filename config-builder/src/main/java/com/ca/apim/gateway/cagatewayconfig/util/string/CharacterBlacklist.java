/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.environment.CharacterBlacklist.getCharBlacklist;

public class CharacterBlacklistFilter {

    private CharacterBlacklistFilter() {
    }

    public static String encodePath(String pathToEncode) {
        if (pathToEncode.contains("_¯") || pathToEncode.contains("¯_")) {
            throw new IllegalArgumentException("Illegal characters in path. Cannot contain '_¯' or '¯_': " + pathToEncode);
        }

        Set<Character> charBlacklist = getCharBlacklist();

        for (char c : pathToEncode.toCharArray()) {
            if (charBlacklist.contains(c)) {
                pathToEncode = pathToEncode.replace(Character.toString(c),"-");
            }
        }

        return pathToEncode;
    }

    public static String decodePath(String pathToDecode) {
        pathToDecode = pathToDecode.replaceAll("_¯", "/");
        pathToDecode = pathToDecode.replaceAll("¯_", "\\\\");
        getCharBlacklist();
        return pathToDecode;
    }

    public static boolean containsInvalidCharacter(String name) {
        Set<Character> charBlacklist = getCharBlacklist();

        for (char c : name.toCharArray()) {
            if (charBlacklist.contains(c)) {
                return true;
            }
        }

        return false;
    }
}
