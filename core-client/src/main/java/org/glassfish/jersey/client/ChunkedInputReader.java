/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * {@link javax.ws.rs.ext.MessageBodyWriter} for {@link ChunkedInput}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@ConstrainedTo(RuntimeType.CLIENT)
class ChunkedInputReader implements MessageBodyReader<ChunkedInput> {

    @Inject
    private Provider<MessageBodyWorkers> messageBodyWorkers;
    @Inject
    private Provider<PropertiesDelegate> propertiesDelegateProvider;

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
                inputStream,
                annotations,
                mediaType,
                headers,
                messageBodyWorkers.get(),
                propertiesDelegateProvider.get());
    }
}
