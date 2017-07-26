package bloomFilter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BloomFilter {
    private final long[] hashes = new long[2];
    private int numHashes;
    private int numElements;
    private long numBits;
    private int numPages;
    private int numBitsPerPage;
    private File bloomFile;
    private CacheBuffer cache;
    private PageBuffer[] pages;
    private final static long SEED = 0L;

    private boolean isActivated = false;

    public BloomFilter(String filePath) throws IOException {
        this.bloomFile = new File(filePath);
        this.numBitsPerPage = CacheBuffer.getPageSize() * 8;
    }

    public void activate() throws IOException {
        if (bloomFile.exists()) {
            this.numHashes = cache.getNumHashes();
            this.numElements = cache.getNumElements();
            this.numPages = cache.getNumPages();
            this.numBits = cache.getNumBits();
            this.cache = new CacheBuffer(bloomFile);
            isActivated = true;
        }
    }

    public void close() throws IOException {
        if (isActivated) {
            cache.close();
            isActivated = false;
        }
    }

    public void cover() throws IOException {
        close();
        if (bloomFile.exists())
            bloomFile.delete();
        //            bloomFile.delete();
    }

    public int getNumElements() {
        return numElements;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public boolean contains(BloomKey k, long[] hashes) throws IOException {
        return contains(k.getBytes(), hashes);
    }

    public boolean contains(byte[] by, long[] hashes) throws IOException {
        if (numPages == 0) {
            return false;
        }
        if (!isActivated) {
            throw new IOException("The bloomfilter is not activated!");
        }
        HashTo128Bit.hashto128(by, SEED, hashes);
        for (int i = 0; i < numHashes; ++i) {
            long hash = Math.abs((hashes[0] + i * hashes[1]) % numBits);
            int pageNo = (int) (hash / numBitsPerPage);
            PageBuffer page = cache.getPage(pageNo);
            int byteNo = (int) (hash % numBitsPerPage) >> 3;
            byte b = page.read(byteNo);
            int bitNo = (int) (hash % numBitsPerPage) & 0x07;
            if (!((b & (1L << bitNo)) != 0)) {
                return false;
            }
        }
        return true;
    }

    public BloomFilterBuilder creatBuilder(int numElements, int numHashes, int numBitsPerElement) throws IOException {
        return new BloomFilterBuilder(numElements, numHashes, numBitsPerElement);
    }

    public class BloomFilterBuilder {
        public BloomFilterBuilder(int numElements, int numHashes, int numBitsPerElement) throws IOException {
            numElements = numElements;
            numHashes = numHashes;
            numBits = numElements * numBitsPerElement;
            long tmp = (long) Math.ceil(numBits / (double) numBitsPerPage);
            if (tmp > Integer.MAX_VALUE) {
                throw new IOException("Cannot create a bloom filter with his huge number of pages.");
            }
            numPages = (int) tmp;
            pages = new PageBuffer[numPages];
            for (int i = 0; i < numPages; i++) {
                pages[i] = new PageBuffer();
            }
        }

        public void add(BloomKey k, int[] keyFields) throws IOException {
            add(k.getBytes());
        }

        public void add(byte[] by) throws IOException {
            if (numPages == 0) {
                throw new IOException(
                        "Cannot add elements to this filter since it is supposed to be empty (number of elements hint passed to the filter during construction was 0).");
            }
            if (isActivated) {
                throw new IOException("The bloomfilter already exists!");
            }
            HashTo128Bit.hashto128(by, SEED, hashes);
            for (int i = 0; i < numHashes; ++i) {
                long hash = Math.abs((hashes[0] + i * hashes[1]) % numBits);
                int pageNo = (int) hash / numBitsPerPage;
                int byteNo = (int) (hash % numBitsPerPage) >> 3;
                byte b = pages[pageNo].read(byteNo);
                int bitNo = (int) (hash % numBitsPerPage) & 0x07;
                b = (byte) (b | (1 << bitNo));
                pages[pageNo].put(byteNo, b);
            }
        }

        public boolean contains(BloomKey k, long[] hashes) throws IOException {
            return contains(k.getBytes(), hashes);
        }

        public boolean contains(byte[] by, long[] hashes) throws IOException {
            if (numPages == 0) {
                return false;
            }
            if (!isActivated) {
                throw new IOException("The bloomfilter is not activated!");
            }
            HashTo128Bit.hashto128(by, SEED, hashes);
            for (int i = 0; i < numHashes; ++i) {
                long hash = Math.abs((hashes[0] + i * hashes[1]) % numBits);
                int pageNo = (int) (hash / numBitsPerPage);
                int byteNo = (int) (hash % numBitsPerPage) >> 3;
                byte b = pages[pageNo].read(byteNo);
                int bitNo = (int) (hash % numBitsPerPage) & 0x07;
                if (!((b & (1L << bitNo)) != 0)) {
                    return false;
                }
            }
            return true;
        }

        public void write() throws IOException {
            write(bloomFile);
        }

        public void write(File file) throws IOException {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(intToBytes(numHashes));
            out.write(intToBytes(numElements));
            out.write(intToBytes(numPages));
            out.write(longToBytes(numBits));
            for (int i = 0; i < numPages; i++) {
                out.write(pages[i].getBuffer());
            }
            out.close();
        }

        private byte[] intToBytes(int v) {
            byte[] dest = new byte[4];
            dest[0] = (byte) (v & 0xFF);
            for (int k = 1; k < 4; k++) {
                dest[k] = (byte) ((v >> 8) & 0xFF);
                v = v >> 8;
            }
            return dest;
        }

        private byte[] longToBytes(long v) {
            byte[] dest = new byte[8];
            dest[0] = (byte) (v & 0xFF);
            for (int k = 1; k < 8; k++) {
                dest[k] = (byte) ((v >> 8) & 0xFF);
                v = v >> 8;
            }
            return dest;
        }
    }
}
