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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Abstract entity provider (reader and writer) base class.
 *
 * @param <T> Java type supported by the provider
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractMessageReaderWriterProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    // TODO: refactor away all constants & static wrappers of ReaderWriter methods and constants - those can be used directly.

    /**
     * The UTF-8 Charset.
     */
    public static final Charset UTF8 = ReaderWriter.UTF8;

    /**
     * Reader bytes from an input stream and write then to an output stream.
     *
     * @param in  the input stream to read from.
     * @param out the output stream to write to.
     * @throws IOException if there is an error reading or writing bytes.
     */
    public static void writeTo(InputStream in, OutputStream out) throws IOException {
        ReaderWriter.writeTo(in, out);
    }

    /**
     * Reader characters from an input stream and write then to an output stream.
     *
     * @param in  the reader to read from.
     * @param out the writer to write to.
     * @throws IOException if there is an error reading or writing characters.
     */
    public static void writeTo(Reader in, Writer out) throws IOException {
        ReaderWriter.writeTo(in, out);
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
        return ReaderWriter.getCharset(m);
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
        return ReaderWriter.readFromAsString(in, type);
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
        ReaderWriter.writeToAsString(s, out, type);
    }

    // MessageBodyWriter
    @Override
    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }
}
