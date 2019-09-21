/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.MessageProperties;

/**
 * A utility class for reading and writing using byte and character streams.
 * <p>
 * If a byte or character array is utilized then the size of the array
 * is by default the value of {@value org.glassfish.jersey.message.MessageProperties#IO_DEFAULT_BUFFER_SIZE}.
 * This value can be set using the system property
 * {@value org.glassfish.jersey.message.MessageProperties#IO_BUFFER_SIZE}.
 *
 * @author Paul Sandoz
 */
public final class ReaderWriter {

    private static final Logger LOGGER = Logger.getLogger(ReaderWriter.class.getName());
    /**
     * The UTF-8 Charset.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");
    /**
     * The buffer size for arrays of byte and character.
     */
    public static final int BUFFER_SIZE = getBufferSize();

    private static int getBufferSize() {
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
                                + " Reverting to default [" + MessageProperties.IO_DEFAULT_BUFFER_SIZE + "].",
                        e);
            }
        }
        return MessageProperties.IO_DEFAULT_BUFFER_SIZE;
    }

    /**
     * Read bytes from an input stream and write them to an output stream.
     *
     * @param in  the input stream to read from.
     * @param out the output stream to write to.
     * @throws IOException if there is an error reading or writing bytes.
     */
    public static void writeTo(InputStream in, OutputStream out) throws IOException {
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
     * If the parameter is not present the {@link #UTF8} charset is utilized.
     *
     * @param m the media type.
     * @return the character set.
     */
    public static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        return (name == null) ? UTF8 : Charset.forName(name);
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
        return readFromAsString(new InputStreamReader(in, getCharset(type)));
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
     * Convert a string to bytes and write those bytes to an output stream.
     *
     * @param s    the string to convert to bytes.
     * @param out  the output stream to write to.
     * @param type the media type that determines the character set defining
     *             how to decode bytes to characters.
     * @throws IOException in case of a write failure.
     */
    public static void writeToAsString(String s, OutputStream out, MediaType type) throws IOException {
        Writer osw = new OutputStreamWriter(out, getCharset(type));
        osw.write(s, 0, s.length());
        osw.flush();
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
