package bloomFilter;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/** An {@link Input} for files. */
public class InputFile implements Closeable {

    private FileChannel channel;

    /** Construct for the given file. */
    public InputFile(File file) throws IOException {
        this.channel = new FileInputStream(file).getChannel();
    }

    public long length() throws IOException {
        return channel.size();
    }

    public int read(long position, byte[] b, int start, int len) throws IOException {
        return channel.read(ByteBuffer.wrap(b, start, len), position);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

}
