package com.tanodxyz.documentrenderer.source;

/**
 * Represents an abstract source that bytes can be read from.  This class forms the foundation for all byte input in GDR.
 * Implementations do not keep track of a current 'position', but rather provide absolute get methods.  Tracking position
 * should be handled in classes that use RandomAccessSource internally (via composition).
 */
public interface IRandomAccessSource {
    /**
     * Gets a byte at the specified position
     *
     * @param position byte position
     * @return the byte, or -1 if EOF is reached
     * @throws java.io.IOException in case of any reading error.
     */
    int get(long position) throws java.io.IOException;

    /**
     * Read an array of bytes of specified length from the specified position of source to the buffer applying the offset.
     * If the number of bytes requested cannot be read, all the possible bytes will be read to the buffer,
     * and the number of actually read bytes will be returned.
     *
     * @param position the position in the RandomAccessSource to read from
     * @param bytes output buffer
     * @param off offset into the output buffer where results will be placed
     * @param len the number of bytes to read
     * @return the number of bytes actually read, or -1 if the file is at EOF
     * @throws java.io.IOException in case of any I/O error.
     */
    int get(long position, byte[] bytes, int off, int len) throws java.io.IOException;

    /**
     * Gets the length of the source
     *
     * @return the length of this source
     */
    long length();

    /**
     * Closes this source. The underlying data structure or source (if any) will also be closed
     *
     * @throws java.io.IOException in case of any reading error.
     */
    void close() throws java.io.IOException;
}