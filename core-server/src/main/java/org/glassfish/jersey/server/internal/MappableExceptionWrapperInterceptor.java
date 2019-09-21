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

package org.glassfish.jersey.server.internal;

import java.io.IOException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.internal.process.MappableException;

/**
 * Interceptor that wraps specific exceptions types thrown by wrapped interceptors and by message
 * body readers and writers into a mappable exception.
 * It must have the lowest priority in order to wrap all other interceptors.
 *
 * @author Miroslav Fuksa
 */
@Priority(10)
@Singleton
public class MappableExceptionWrapperInterceptor implements ReaderInterceptor, WriterInterceptor {

    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) throws IOException, WebApplicationException {
        try {
            return context.proceed();
        } catch (final WebApplicationException | MappableException | MessageBodyProviderNotFoundException e) {
            throw e;
        } catch (final Exception e) {
            throw new MappableException(e);
        }

    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            context.proceed();
        } catch (final WebApplicationException | MappableException e) {
            throw e;
        } catch (final MessageBodyProviderNotFoundException nfe) {
            throw new InternalServerErrorException(nfe);
        } catch (final Exception e) {
            throw new MappableException(e);
        }

    }

    /**
     * Binder registering the {@link MappableExceptionWrapperInterceptor Exception Wrapper Interceptor}
     * (used on the client side).
     *
     */
    public static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(MappableExceptionWrapperInterceptor.class)
                    .to(ReaderInterceptor.class)
                    .to(WriterInterceptor.class)
                    .in(Singleton.class);
        }
    }
}
