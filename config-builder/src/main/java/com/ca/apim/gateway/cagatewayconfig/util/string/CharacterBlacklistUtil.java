/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.environment.CharacterBlacklist.getCharBlacklist;

/**
 * Utility class for Character Blacklist. Purpose is to apply functions to the constants found in CharacterBlacklist
 */
public class CharacterBlacklistUtil {

    /**
     * Find blacklist characters and replace with value '-'
     *
     * @param string with invalid characters
     * @return a string stripped of all invalid characters and replaced with value '-'
     */
    public static String filterAndReplace(String string) {
        Set<Character> charBlacklist = getCharBlacklist();
        final char replacementChar = '-';
        StringBuilder processedString = new StringBuilder();

        for (char c : string.toCharArray()) {
            if (charBlacklist.contains(c)) {
                processedString.append(replacementChar);
            } else {
                processedString.append(c);
            }
        }

        return compressString(processedString.toString(), replacementChar);
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

    private CharacterBlacklistUtil() {}
}
