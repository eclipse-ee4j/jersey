/*
 * Copyright (c) 2024 Markus KARG and others.
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

import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.WILDCARD;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import jakarta.inject.Singleton;

/**
 * Provider for marshalling/un-marshalling of {@code application/octet-stream}
 * entity type to/from a {@link Path} instance.
 *
 * @author Markus KARG
 */
@Produces({APPLICATION_OCTET_STREAM, WILDCARD})
@Consumes({APPLICATION_OCTET_STREAM, WILDCARD})
@Singleton
public final class PathProvider extends AbstractMessageReaderWriterProvider<Path> {

    @Override
    public final boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return Path.class == type;
    }

    @Override
    public final Path readFrom(final Class<Path> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream) throws IOException {
        final var path = Utils.createTempFile().toPath();
        Files.copy(entityStream, path, StandardCopyOption.REPLACE_EXISTING);
        return path;
    }

    @Override
    public final boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return Path.class.isAssignableFrom(type);
    }

    @Override
    public final void writeTo(final Path t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException {
        Files.copy(t, entityStream);
    }
}
