/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * Utility methods for policy simplification
 */
public class PolicySimplifierUtils {

    /**
     * Decode a Base64 string to byte arrays, using UTF-8 as encoding.
     *
     * @param base64Expression the base64 compressed string
     * @return byte array with the decoded text
     */
    public static byte[] base64Decode(String base64Expression) {
        try {
            return Base64.decodeBase64(base64Expression.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to decode: " + base64Expression, e);
        }
    }

    private PolicySimplifierUtils() {}
}
