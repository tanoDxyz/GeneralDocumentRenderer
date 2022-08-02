package com.tanodxyz.documentrenderer.source;

import java.io.InputStream;

/**
 * An input stream that uses a {@link IRandomAccessSource} as
 * its underlying source.
 */
public class RASInputStream extends InputStream {

    /**
     * The source.
     */
    private final IRandomAccessSource source;

    /**
     * The current position in the source.
     */
    private long position = 0;

    /**
     * Creates an input stream based on the source.
     * @param source The source.
     */
    public RASInputStream(IRandomAccessSource source){
        this.source = source;
    }

    /**
     * Gets the source
     *
     * @return an instance of {@link IRandomAccessSource}
     */
    public IRandomAccessSource getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws java.io.IOException {
        int count = source.get(position, b, off, len);
        position += count;
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws java.io.IOException {
        return source.get(position++);
    }
}