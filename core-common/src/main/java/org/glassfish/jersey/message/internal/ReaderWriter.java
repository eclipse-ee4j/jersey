/*
 * Copyright (c) 2010, 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.message.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.MessageProperties;

/**
 * A utility class for reading and writing using byte and character streams.
 * <p>
 * If a byte or character array is utilized then the size of the array
 * is by default decided by the JRE.
 * This value can be set using the system property
 * {@value org.glassfish.jersey.message.MessageProperties#IO_BUFFER_SIZE}.
 *
 * @author Paul Sandoz
 */
public final class ReaderWriter {

    private static final Logger LOGGER = Logger.getLogger(ReaderWriter.class.getName());

    /**
     * The buffer size for arrays of byte and character.
     */
    public static final int BUFFER_SIZE = getBufferSize();

    /**
     * Whether {@linkplain #BUFFER_SIZE} is to be ignored in favor of JRE's own decision.
     */
    public static final boolean AUTOSIZE_BUFFER = getAutosizeBuffer();

    private static int getIOBufferSize() {
        // TODO should we unify this buffer size and CommittingOutputStream buffer size (controlled by CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER)?
        final String value = AccessController.doPrivileged(PropertiesHelper.getSystemProperty(MessageProperties.IO_BUFFER_SIZE));
        if (value != null) {
            try {
                final int i = Integer.parseInt(value);
                if (i <= 0) {
                    throw new NumberFormatException("Value not positive.");
                }
                return i;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.CONFIG,
                        "Value of " + MessageProperties.IO_BUFFER_SIZE
                                + " property is not a valid positive integer [" + value + "]."
                                + " Reverting to default [at JRE's discretion].",
                        e);
            }
        }
        return -1;
    }

    private static int getBufferSize() {
        final int ioBufferSize = getIOBufferSize();
        return ioBufferSize == -1 ? MessageProperties.IO_DEFAULT_BUFFER_SIZE : ioBufferSize;
    }

    private static boolean getAutosizeBuffer() {
        return getIOBufferSize() == -1;
    }

    /**
     * Read bytes from an input stream and write them to an output stream.
     *
     * @param in  the input stream to read from.
     * @param out the output stream to write to.
     * @throws IOException if there is an error reading or writing bytes.
     */
    public static void writeTo(InputStream in, OutputStream out) throws IOException {
        if (AUTOSIZE_BUFFER) {
            in.transferTo(out);
            return;
        }

        int read;
        final byte[] data = new byte[BUFFER_SIZE];
        while ((read = in.read(data)) != -1) {
            out.write(data, 0, read);
        }
    }

    /**
     * Read characters from an input stream and write them to an output stream.
     *
     * @param in  the reader to read from.
     * @param out the writer to write to.
     * @throws IOException if there is an error reading or writing characters.
     */
    public static void writeTo(Reader in, Writer out) throws IOException {
        if (AUTOSIZE_BUFFER) {
            in.transferTo(out);
            return;
        }

        int read;
        final char[] data = new char[BUFFER_SIZE];
        while ((read = in.read(data)) != -1) {
            out.write(data, 0, read);
        }
    }

    /**
     * Get the character set from a media type.
     * <p>
     * The character set is obtained from the media type parameter "charset".
     * If the parameter is not present the {@link StandardCharsets#UTF_8} charset is utilized.
     *
     * @param m the media type.
     * @return the character set.
     */
    public static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? StandardCharsets.UTF_8 : Charset.forName(name);
    }

    /**
     * Read the bytes of an input stream and convert to a string.
     *
     * @param in   the input stream to read from.
     * @param type the media type that determines the character set defining
     *             how to decode bytes to characters.
     * @return the string.
     *
     * @throws IOException if there is an error reading from the input stream.
     */
    public static String readFromAsString(InputStream in, MediaType type) throws IOException {
        return new String(readAllBytes(in), getCharset(type));
    }

    /**
     * Read the characters of a reader and convert to a string.
     *
     * @param reader the reader
     * @return the string
     *
     * @throws IOException if there is an error reading from the reader.
     */
    public static String readFromAsString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] c = new char[BUFFER_SIZE];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }
    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Java 9+ InputStream::readAllBytes
     * TODO Replace in Jersey 4.0, as the sole difference to OpenJDK is working around a bug in the input stream.
     */
    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = Integer.MAX_VALUE;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = inputStream.read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;

                if (nread == BUFFER_SIZE) { // This differs from JDK version
                    break;                  // prevents a bug (See ReaderWriterTest)
                }
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                if (nread < buf.length) {
                    buf = Arrays.copyOfRange(buf, 0, nread);
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ? result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }

    /**
     * Convert a string to bytes and write those bytes to an output stream.
     *
     * @param s    the string to convert to bytes.
     * @param out  the output stream to write to.
     * @param type the media type that determines the character set defining
     *             how to decode bytes to characters.
     * @throws IOException in case of a write failure.
     */
    public static void writeToAsString(String s, OutputStream out, MediaType type) throws IOException {
        out.write(s.getBytes(getCharset(type)));
    }

    /**
     * Safely close a closeable, without throwing an exception.
     *
     * @param closeable object to be closed.
     */
    public static void safelyClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ioe) {
            LOGGER.log(Level.FINE, LocalizationMessages.MESSAGE_CONTENT_INPUT_STREAM_CLOSE_FAILED(), ioe);
        } catch (ProcessingException pe) {
            LOGGER.log(Level.FINE, LocalizationMessages.MESSAGE_CONTENT_INPUT_STREAM_CLOSE_FAILED(), pe);
        }
    }

    /**
     * Prevents instantiation.
     */
    private ReaderWriter() {
    }
}
