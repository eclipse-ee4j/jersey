/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity.filtering;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.inject.Inject;

import org.glassfish.jersey.message.filtering.spi.FilteringHelper;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

/**
 * @author Michal Gajdos
 */
@Provider
@Consumes("entity/filtering")
@Produces("entity/filtering")
public class FilteringMessageBodyProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private static final Logger LOGGER = Logger.getLogger(FilteringMessageBodyProvider.class.getName());

    @Inject
    private javax.inject.Provider<ObjectProvider<ObjectGraph>> provider;

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType) {
        return String.class != type;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return String.class != type;
    }

    @Override
    public long getSize(final Object o, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations,
                           final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                           final InputStream entityStream) throws IOException, WebApplicationException {
        try {
            final ObjectGraph objectGraph = provider.get()
                    .getFilteringObject(FilteringHelper.getEntityClass(genericType), false, annotations);

            return objectGraphToString(objectGraph);
        } catch (final Throwable t) {
            LOGGER.log(Level.WARNING, "Error during reading an object graph.", t);
            return "ERROR: " + t.getMessage();
        }
    }

    @Override
    public void writeTo(final Object o, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        final ObjectGraph objectGraph = provider.get()
                .getFilteringObject(FilteringHelper.getEntityClass(genericType), true, annotations);

        try {
            entityStream.write(objectGraphToString(objectGraph).getBytes());
        } catch (final Throwable t) {
            LOGGER.log(Level.WARNING, "Error during writing an object graph.", t);
        }
    }

    private static String objectGraphToString(final ObjectGraph objectGraph) {
        final StringBuilder sb = new StringBuilder();
        for (final String field : objectGraphToFields("", objectGraph)) {
            if (!field.contains("Transient")) {
                sb.append(field).append(',');
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private static List<String> objectGraphToFields(final String prefix, final ObjectGraph objectGraph) {
        final List<String> fields = new ArrayList<>();

        // Fields.
        for (final String field : objectGraph.getFields()) {
            fields.add(prefix + field);
        }

        for (final Map.Entry<String, ObjectGraph> entry : objectGraph.getSubgraphs().entrySet()) {
            fields.addAll(objectGraphToFields(prefix + entry.getKey() + ".", entry.getValue()));
        }

        return fields;
    }
}
