/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import jakarta.inject.Singleton;

/**
 * Provider for marshalling/un-marshalling of {@code application/octet-stream}
 * entity type to/from a {@link File} instance.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
@Produces({"application/octet-stream", "*/*"})
@Consumes({"application/octet-stream", "*/*"})
@Singleton
public final class FileProvider extends AbstractMessageReaderWriterProvider<File> {

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return File.class == type;
    }

    @Override
    public File readFrom(final Class<File> type,
                         final Type genericType,
                         final Annotation[] annotations,
                         final MediaType mediaType,
                         final MultivaluedMap<String, String> httpHeaders,
                         final InputStream entityStream) throws IOException {
        final File file = Utils.createTempFile();

        Files.copy(entityStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return file;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return File.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(final File t,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException {
        Files.copy(t.toPath(), entityStream);
    }

    @Override
    public long getSize(final File t,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return t.length();
    }
}
