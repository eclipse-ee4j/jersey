/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.kryo.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * The KryoMessageBodyProvider expects a {@code ContextResolver<Kryo>} registered.
 * @author Libor Kramolis
 */
@Provider
@Consumes("application/x-kryo")
@Produces("application/x-kryo")
public class KryoMessageBodyProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    private final ContextResolver<Kryo> contextResolver;
    private final Optional<KryoPool> kryoPool;

    public KryoMessageBodyProvider(@Context Providers providers) {
        final MediaType mediaType = new MediaType("application", "x-kryo");
        contextResolver = providers.getContextResolver(Kryo.class, mediaType);
        kryoPool = getKryoPool();
    }

    private Kryo getKryo() {
        return contextResolver != null ? contextResolver.getContext(null) : null;
    }

    private Optional<KryoPool> getKryoPool() {
        final Kryo kryo = getKryo();
        final KryoFactory kryoFactory = new KryoFactory() {
            public Kryo create() {
                return kryo;
            }
        };
        return kryo == null ? Optional.empty() : Optional.of(new KryoPool.Builder(kryoFactory).softReferences().build());
    }

    //
    // MessageBodyWriter
    //

    @Override
    public long getSize(final Object object, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
                               final Annotation[] annotations, final MediaType mediaType) {
        return kryoPool != null;
    }

    @Override
    public void writeTo(final Object object, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
            throws IOException, WebApplicationException {
        final Output output = new Output(entityStream);
        kryoPool.ifPresent(a -> a.run(new KryoCallback() {
            public Object execute(Kryo kryo) {
                kryo.writeObject(output, object);
                return null;
            }
        }));
        output.flush();
    }

    //
    // MessageBodyReader
    //

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType,
                              final Annotation[] annotations, final MediaType mediaType) {
        return kryoPool != null;
    }

    @Override
    public Object readFrom(final Class<Object> type, final Type genericType,
                           final Annotation[] annotations, final MediaType mediaType,
                           final MultivaluedMap<String, String> httpHeaders,
                           final InputStream entityStream) throws IOException, WebApplicationException {
        final Input input = new Input(entityStream);

        return kryoPool.map(pool -> pool.run(new KryoCallback() {
            public Object execute(Kryo kryo) {
                return kryo.readObject(input, type);
            }
        })).orElse(null);
    }

}
