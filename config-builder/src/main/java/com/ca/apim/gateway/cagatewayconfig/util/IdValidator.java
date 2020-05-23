package com.ca.apim.gateway.cagatewayconfig.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdValidator {
    private IdValidator() {
    }

    static final Pattern PAT_TRAILING_HEX = Pattern.compile("[0-9a-f]+$", Pattern.CASE_INSENSITIVE);
    static final String[] ZEROS;
    static final String[] ONES;

    static {
        StringBuilder sbz = new StringBuilder();
        StringBuilder sbf = new StringBuilder();
        String[] z = new String[17];
        String[] f = new String[17];
        for (int i = 0; i <= 16; ++i) {
            z[i] = sbz.toString();
            sbz.append("0");

            f[i] = sbf.toString();
            sbf.append("f");
        }

        ZEROS = z;
        ONES = f;
    }

    static String decompressString(@NotNull final String goidString) {
        if (goidString.length() > 32) {
            throw new IllegalArgumentException("Invalid GOID (too long)");
        }

        StringBuilder sb = new StringBuilder();
        String string = goidString;
        int segs = 0;

        while (string.length() > 0) {
            Matcher m = PAT_TRAILING_HEX.matcher(string);
            if (m.find()) {
                string = m.replaceFirst("");
                sb.insert(0, m.group(0));
            } else if (string.endsWith("n") || string.endsWith("N")) {
                segs++;
                string = string.substring(0, string.length() - 1);
                int neededNybbles = 16 - sb.length() % 16;
                sb.insert(0, ONES[neededNybbles]);
            } else if (string.endsWith("z") || string.endsWith("Z")) {
                segs++;
                string = string.substring(0, string.length() - 1);
                int neededNybbles = 16 - sb.length() % 16;
                sb.insert(0, ZEROS[neededNybbles]);
            } else {
                throw new IllegalArgumentException("Invalid Goid (unrecognized suffix)");
            }

            if (segs > 2)
                throw new IllegalArgumentException("Invalid Goid (too many segments)");
        }

        return sb.toString();
    }

    public static boolean isValidGoid(final String goid) {
        try {
            final String goidHex;
            if (goid.length() == 32) {
                // Bypass decompression for performance in common case
                goidHex = goid;
            } else {
                goidHex = decompressString(goid);
            }

            byte[] goidFromString;
            try {
                goidFromString = unHexDump(goidHex);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot create goid from this String. Invalid hex data: " + goid);
            }

            if (goidFromString.length != 16) {
                throw new IllegalArgumentException("Cannot create a goid from this String, it does not decode to a 16 byte array.");
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isValidGuid(final String guid) {
        try {
            UUID.fromString(guid);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Convert a string of hexidecimal digits in the form "FF0E7BCC"... into a byte array.
     * The example would return the byte array { 0xFF, 0x0E, 0x7b, 0xCC }.  This is the inverse
     * of the operation performed by hexDump().
     *
     * @param hexData the string containing the hex data to decode.  May not contain whitespace.
     * @return the decoded byte array, which may be zero length but is never null.
     * @throws IOException if the input string contained characters other than '0'..'9', 'a'..'f', 'A'..'F'.
     */
    public static byte[] unHexDump(String hexData) throws IOException {
        if (hexData.length() % 2 != 0) throw new IOException("String must be of even length");
        byte[] bytes = new byte[hexData.length() / 2];
        for (int i = 0; i < hexData.length(); i += 2) {
            int b1 = nybble(hexData.charAt(i));
            int b2 = nybble(hexData.charAt(i + 1));
            bytes[i / 2] = (byte) ((b1 << 4) + b2);
        }
        return bytes;
    }

    private static byte nybble(char hex) throws IOException {
        if (hex <= '9' && hex >= '0') {
            return (byte) (hex - '0');
        } else if (hex >= 'a' && hex <= 'f') {
            return (byte) (hex - 'a' + 10);
        } else if (hex >= 'A' && hex <= 'F') {
            return (byte) (hex - 'A' + 10);
        } else {
            throw new IOException("Invalid hex digit " + hex);
        }
    }
}
