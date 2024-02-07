package org._ubb.utils;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HexFormat;

@UtilityClass
public class MD5 {
    private static final int INIT_A = 0x67452301;
    private static final int INIT_B = 0xEFCDAB89;
    private static final int INIT_C = 0x98BADCFE;
    private static final int INIT_D = 0x10325476;
    private static final int[] SHIFTS = {7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21};
    private static final int[] TABLE_K = new int[64];

    static {
        for (int i = 0; i < 64; i++)
            TABLE_K[i] = (int) (Math.pow(2, 32) * Math.abs(Math.sin(i + 1)));
    }

    public static byte[] computeMD5(byte[] message) {
        ByteBuffer padded = ByteBuffer.allocate((((message.length + 8) / 64) + 1) * 64).order(ByteOrder.LITTLE_ENDIAN);
        padded.put(message);
        padded.put((byte) 10000000);
        long messageLenBits = (long) message.length * 8;
        padded.putLong(padded.capacity() - 8, messageLenBits);
        padded.rewind();

        int a = INIT_A;
        int b = INIT_B;
        int c = INIT_C;
        int d = INIT_D;
        while (padded.hasRemaining()) {
            IntBuffer chunk = padded.slice().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
            int originalA = a;
            int originalB = b;
            int originalC = c;
            int originalD = d;
            for (int j = 0; j < 64; j++) {
                int div16 = j / 16;
                int f = 0;
                int bufferIndex = j;
                switch (div16) {
                    case 0:
                        f = (b & c) | (~b & d);
                        break;
                    case 1:
                        f = (b & d) | (c & ~d);
                        bufferIndex = (bufferIndex * 5 + 1) % 16;
                        break;
                    case 2:
                        f = b ^ c ^ d;
                        bufferIndex = (bufferIndex * 3 + 5) % 16;
                        break;
                    case 3:
                        f = c ^ (b | ~d);
                        bufferIndex = (bufferIndex * 7) % 16;
                        break;
                }
                int F = f + a + TABLE_K[j] + chunk.get(bufferIndex);
                int temp = Integer.rotateLeft(F, SHIFTS[div16 * 4 + j % 4]);
                a = d;
                d = c;
                c = b;
                b = b + temp;
            }
            a += originalA;
            b += originalB;
            c += originalC;
            d += originalD;
            padded.position(padded.position() + 64);
        }

        ByteBuffer md5 = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        for (int n : new int[]{a, b, c, d}) {
            md5.putInt(n);
        }
        return md5.array();
    }

    public static String md5ToHexString(byte[] b) {
        return HexFormat.of().formatHex(computeMD5(b));
    }
}