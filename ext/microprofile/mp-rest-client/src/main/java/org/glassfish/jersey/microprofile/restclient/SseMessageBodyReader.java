/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Consumes;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.ReaderInterceptorExecutor;
import org.reactivestreams.Publisher;

@Consumes(MediaType.SERVER_SENT_EVENTS)
@ConstrainedTo(RuntimeType.CLIENT)
public class SseMessageBodyReader implements MessageBodyReader<Publisher<InboundEvent>> {

    @Context
    protected Providers providers;

    @Inject
    private Provider<MessageBodyWorkers> messageBodyWorkers;

    @Inject
    private Provider<PropertiesDelegate> propertiesDelegateProvider;

    @Inject
    private Provider<ExecutorService> executorServiceProvider;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Publisher.class.isAssignableFrom(type)
                && MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType);
    }

    @Override
    public Publisher<InboundEvent> readFrom(Class<Publisher<InboundEvent>> chunkedInputClass,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> headers,
            InputStream inputStream) throws IOException, WebApplicationException {
        InputStream closeableInputStream = ReaderInterceptorExecutor.closeableInputStream(inputStream);
        return new SseEventPublisher(
                closeableInputStream,
                genericType,
                annotations,
                mediaType,
                headers,
                messageBodyWorkers.get(),
                propertiesDelegateProvider.get(),
                executorServiceProvider.get()
        );
    }
}
