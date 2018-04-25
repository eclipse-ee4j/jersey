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

package org.glassfish.jersey.server;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.WriterInterceptor;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.internal.JsonWithPaddingInterceptor;
import org.glassfish.jersey.server.internal.MappableExceptionWrapperInterceptor;
import org.glassfish.jersey.server.internal.monitoring.MonitoringContainerListener;

/**
 * Server injection binder.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
class ServerBinder extends AbstractBinder {

    @Override
    protected void configure() {
        install(new MappableExceptionWrapperInterceptor.Binder(),
                new MonitoringContainerListener.Binder());

        //ChunkedResponseWriter
        bind(ChunkedResponseWriter.class).to(MessageBodyWriter.class).in(Singleton.class);

        // JSONP
        bind(JsonWithPaddingInterceptor.class).to(WriterInterceptor.class).in(Singleton.class);
    }
}
