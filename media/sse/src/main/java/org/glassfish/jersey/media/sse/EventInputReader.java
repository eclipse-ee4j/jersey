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

package org.glassfish.jersey.media.sse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.MessageUtils;
import org.glassfish.jersey.message.internal.ReaderInterceptorExecutor;

/**
 * SSE {@link EventInput event input} message body reader.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class EventInputReader implements MessageBodyReader<EventInput> {

    @Inject
    private Provider<MessageBodyWorkers> messageBodyWorkers;
    @Inject
    private Provider<PropertiesDelegate> propertiesDelegateProvider;

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass.equals(EventInput.class);
    }

    @Override
    public EventInput readFrom(Class<EventInput> chunkedInputClass,
                                 Type type,
                                 Annotation[] annotations,
                                 MediaType mediaType,
                                 MultivaluedMap<String, String> headers,
                                 InputStream inputStream) throws IOException, WebApplicationException {
        InputStream closeableInputStream = ReaderInterceptorExecutor.closeableInputStream(inputStream);
        return new EventInput(
                closeableInputStream,
                annotations,
                mediaType,
                headers,
                messageBodyWorkers.get(),
                propertiesDelegateProvider.get());
    }
}
