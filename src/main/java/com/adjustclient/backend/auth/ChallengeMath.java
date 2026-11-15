package com.adjustclient.backend.auth;

import java.math.BigInteger;

public class ChallengeMath {

    public static byte[] a(byte[] encoded, byte[] keyData) {
        int len1 = ((keyData[0] & 0xFF) << 8) | (keyData[1] & 0xFF);
        byte[] part1 = new byte[len1];
        int len2 = keyData.length - 2 - len1;
        byte[] part2 = new byte[len2];
        for (int i = 0; i < len1; i++) {
            part1[i] = (byte) (keyData[2 + i] ^ (i * len1));
        }
        if (len2 != 0) {
            part2[0] = (byte) (keyData[2 + len1] ^ 0xF1);
            for (int i = 1; i < len2; i++) {
                part2[i] = (byte) (keyData[2 + len1 + i] ^ (-5 * (i + 3) + i * len2));
            }
        }
        return new BigInteger(encoded).modPow(new BigInteger(part1), new BigInteger(part2)).toByteArray();
    }

    public static byte[] c(byte[] first, byte[] second) {
        int len1 = first.length;
        int len2 = second.length;
        byte[] out = new byte[len1 + len2 + 2];
        out[0] = (byte) (len1 >>> 8);
        out[1] = (byte) len1;
        for (int i = 0; i < len1; i++) {
            out[2 + i] = (byte) (first[i] ^ (i * len1));
        }
        for (int i = 0; i < len2; i++) {
            out[2 + len1 + i] = (byte) (second[i] ^ (-5 * (i + 3) + i * len2));
        }
        return out;
    }
}
