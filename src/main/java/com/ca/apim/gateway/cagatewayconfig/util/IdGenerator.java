/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private AtomicLong hi;
    private AtomicLong low;
    private static final int MAX_GOID_RESERVED_PREFIX = 65536;

    public IdGenerator() {
        final Random random = new SecureRandom();

        long randomHi;
        do {
            randomHi = random.nextLong();
            // make sure hi cannot be in the range of default prefixes 0 - 2^16
        } while (randomHi >= 0 && randomHi < MAX_GOID_RESERVED_PREFIX);

        hi = new AtomicLong(randomHi);
        low = new AtomicLong(random.nextLong());
    }

    public String generate() {
        //Do not need to increment hi on low rollover. Preforming the increment could lead to race conditions without proper locking.
        // Also it is extremely unlikely a gateway will create 2^64 entities without restarting.
        return hexDump(ByteBuffer.allocate(16).putLong(hi.get()).putLong(low.getAndIncrement()).array());
    }

    private static String hexDump(byte[] binaryData) {
        return hexDump(binaryData, 0, binaryData.length);
    }

    private static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String hexDump(byte[] binaryData, int off, int len) {
        if (binaryData == null) throw new NullPointerException();
        if (off < 0 || len < 0 || off + len > binaryData.length) throw new IllegalArgumentException();
        char[] buffer = new char[len * 2];
        for (int i = 0; i < len; i++) {
            int low = (binaryData[off + i] & 0x0f);
            int high = ((binaryData[off + i] & 0xf0) >> 4);
            buffer[i * 2] = hexadecimal[high];
            buffer[i * 2 + 1] = hexadecimal[low];
        }
        return new String(buffer);
    }
}
