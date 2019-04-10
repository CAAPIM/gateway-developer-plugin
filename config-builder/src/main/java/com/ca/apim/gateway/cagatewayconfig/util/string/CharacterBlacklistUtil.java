/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.environment.CharacterBlacklist.getCharBlacklist;

public class CharacterBlacklistUtil {

    public static String filterAndReplace(String string) {
        Set<Character> charBlacklist = getCharBlacklist();

        for (char c : string.toCharArray()) {
            if (charBlacklist.contains(c)) {
                string = string.replace(Character.toString(c),"-");
            }
        }

        return string;
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

    private CharacterBlacklistUtil() {}
}
