/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.string;

import com.ca.apim.gateway.cagatewayconfig.util.environment.CharacterBlacklist;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterBlacklistUtilTest {

    @Test
    void filterAndReplace() {
        assertEquals("example-slashed.xml", CharacterBlacklistUtil.filterAndReplace("example-/-\\-slashed.xml"));
    }

    @Test
    void stringCompressionOnRepeatingHyphens() {
        assertEquals("example-", CharacterBlacklistUtil.filterAndReplace("example-------"));
    }

    @Test
    void containsInvalidCharacter() {
        Character[] blacklistChar = {CharacterBlacklist.LESS_THAN, CharacterBlacklist.GREATER_THAN, CharacterBlacklist.COLON, CharacterBlacklist.DOUBLE_QUOTE,
            CharacterBlacklist.FORWARD_SLASH, CharacterBlacklist.BACK_SLASH, CharacterBlacklist.PIPE, CharacterBlacklist.QUESTION_MARK, CharacterBlacklist.ASTERISK,
            CharacterBlacklist.NULL_CHAR};

        Stream.of(blacklistChar).forEach(c -> {
            assertTrue(CharacterBlacklistUtil.containsInvalidCharacter("example with invalid character " + c));
        });
    }

    @Test
    void containsNoInvalidCharacter() {
        assertFalse(CharacterBlacklistUtil.containsInvalidCharacter("example with no invalid characters"));
    }
}