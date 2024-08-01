/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.ReaderInterceptorExecutor;

/**
 * {@link jakarta.ws.rs.ext.MessageBodyWriter} for {@link ChunkedInput}.
 *
 * @author Marek Potociar
 */
@ConstrainedTo(RuntimeType.CLIENT)
class ChunkedInputReader implements MessageBodyReader<ChunkedInput> {

    private final Provider<MessageBodyWorkers> messageBodyWorkers;
    private final Provider<PropertiesDelegate> propertiesDelegateProvider;

    @Inject
    public ChunkedInputReader(@Context Provider<MessageBodyWorkers> messageBodyWorkers,
                              @Context Provider<PropertiesDelegate> propertiesDelegateProvider) {
        this.messageBodyWorkers = messageBodyWorkers;
        this.propertiesDelegateProvider = propertiesDelegateProvider;
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass.equals(ChunkedInput.class);
    }

    @Override
    public ChunkedInput readFrom(Class<ChunkedInput> chunkedInputClass,
                                   Type type,
                                   Annotation[] annotations,
                                   MediaType mediaType,
                                   MultivaluedMap<String, String> headers,
                                   InputStream inputStream) throws IOException, WebApplicationException {

        final Type chunkType = ReflectionHelper.getTypeArgument(type, 0);

        return new ChunkedInput(
                chunkType,
                ReaderInterceptorExecutor.closeableInputStream(inputStream),
                annotations,
                mediaType,
                headers,
                messageBodyWorkers.get(),
                propertiesDelegateProvider.get());
    }
}
