package bloomFilter;

public class HashTo128Bit {

    public static long rotl64(long v, int n) {
        return ((v << n) | (v >>> (64 - n)));
    }

    public static long fmix(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;

        return k;
    }

    public static void hashto128(byte[] by, long seed, long[] hashes) {
        //KeyToBytes by = new KeyToBytes(record);
        int length = by.length;
        final int nblocks = length >> 4;

        long h1 = seed;
        long h2 = seed;

        long c1 = 0x87c37b91114253d5L;
        long c2 = 0x4cf5ad432745937fL;

        int currentFieldIndex = 0;
        int bytePos = 0;
        for (int i = 0; i < nblocks; ++i) {
            long k1 = 0L;
            for (int j = 0; j < 8; ++j) {
                k1 += (((long) by[bytePos]) & 0xff) << (j << 3);
                ++bytePos;
            }
            long k2 = 0L;
            for (int j = 0; j < 8; ++j) {
                k2 += (((long) by[bytePos]) & 0xff) << (j << 3);
                ++bytePos;
            }

            k1 *= c1;
            k1 = rotl64(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            h1 = rotl64(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= c2;
            k2 = rotl64(k2, 33);
            k2 *= c1;
            h2 ^= k2;

            h2 = rotl64(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        long k1 = 0L;
        long k2 = 0L;

        bytePos = length - 1;
        switch (length & 15) {
            case 15:
                k2 ^= ((long) by[bytePos]) << 48;
                --bytePos;
            case 14:
                k2 ^= ((long) by[bytePos]) << 40;
                --bytePos;
            case 13:
                k2 ^= ((long) by[bytePos]) << 32;
                --bytePos;
            case 12:
                k2 ^= ((long) by[bytePos]) << 24;
                --bytePos;
            case 11:
                k2 ^= ((long) by[bytePos]) << 16;
                --bytePos;
            case 10:
                k2 ^= ((long) by[bytePos]) << 8;
                --bytePos;
            case 9:
                k2 ^= (long) by[bytePos];
                --bytePos;
                k2 *= c2;
                k2 = rotl64(k2, 33);
                k2 *= c1;
                h2 ^= k2;

            case 8:
                k1 ^= ((long) by[bytePos]) << 56;
                --bytePos;
            case 7:
                k1 ^= ((long) by[bytePos]) << 48;
                --bytePos;
            case 6:
                k1 ^= ((long) by[bytePos]) << 40;
                --bytePos;
            case 5:
                k1 ^= ((long) by[bytePos]) << 32;
                --bytePos;
            case 4:
                k1 ^= ((long) by[bytePos]) << 24;
                --bytePos;
            case 3:
                k1 ^= ((long) by[bytePos]) << 16;
                --bytePos;
            case 2:
                k1 ^= ((long) by[bytePos]) << 8;
                --bytePos;
            case 1:
                k1 ^= (long) by[bytePos];
                --bytePos;
                k1 *= c1;
                k1 = rotl64(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        }

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix(h1);
        h2 = fmix(h2);

        h1 += h2;
        h2 += h1;

        hashes[0] = h1;
        hashes[1] = h2;
    }
}
