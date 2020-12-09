/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This generates ids that are used on gateway entities. It works by randomly generating an id and then incrementing it to retrieve subsequent ids.
 */
public class IdGenerator {

    private AtomicLong hi;
    private AtomicLong low;
    private static final int MAX_ID_RESERVED_PREFIX = 65536;

    /**
     * Creates a new IdGenerator. This will randomly seed the initial id
     */
    public IdGenerator() {
        final Random random = new SecureRandom();

        long randomHi;
        do {
            randomHi = random.nextLong();
            // make sure hi cannot be in the range of default prefixes 0 - 2^16
        } while (randomHi >= 0 && randomHi < MAX_ID_RESERVED_PREFIX);

        hi = new AtomicLong(randomHi);
        low = new AtomicLong(random.nextLong());
    }

    /**
     * Return an id that can be used for a Gateway entity. This id will likely be unique, chance of collision is extremely low.
     *
     * @return An id that can be used for a gateway entity.
     */
    public String generate() {
        // Do not need to increment hi on low rollover. It is extremely unlikely a we will create 2^32 id's on a single id generator in order to cause a collision.
        return hexDump(ByteBuffer.allocate(16).putLong(hi.get()).putLong(low.getAndIncrement()).array());
    }

    private static String hexDump(byte[] binaryData) {
        return hexDump(binaryData, binaryData.length);
    }

    private static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String hexDump(byte[] binaryData, int len) {
        int off = 0;
        if (binaryData == null) throw new NullPointerException();
        if (len < 0 || off + len > binaryData.length) throw new IllegalArgumentException();
        char[] buffer = new char[len * 2];
        for (int i = 0; i < len; i++) {
            int low = (binaryData[off + i] & 0x0f);
            int high = ((binaryData[off + i] & 0xf0) >> 4);
            buffer[i * 2] = hexadecimal[high];
            buffer[i * 2 + 1] = hexadecimal[low];
        }
        return new String(buffer);
    }

    public String generateGuid() {
        return UUID.randomUUID().toString();
    }

    public static String generateGuid(String forName) {
        return generateUUID(forName + "::guid").toString();
    }

    public static String generate(String forName) {
        final UUID uuid = generateUUID(forName + "::id");
        return hexDump(ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array());
    }

    private static UUID generateUUID(String forName) {
        return UUID.nameUUIDFromBytes(forName.getBytes(Charset.forName("utf-8")));
    }
}
