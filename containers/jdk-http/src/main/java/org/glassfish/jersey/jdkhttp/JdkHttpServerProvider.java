/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.jdkhttp;

import javax.ws.rs.JAXRS;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;

import com.sun.net.httpserver.HttpServer;

/**
 * Server provider for servers based on JDK {@link HttpServer}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public final class JdkHttpServerProvider implements ServerProvider {

    @Override
    public final <T extends Server> T createServer(final Class<T> type, final Application application,
            final JAXRS.Configuration configuration) {
        return JdkHttpServer.class == type || Server.class == type
                ? type.cast(new JdkHttpServer(application, configuration))
                : null;
    }
}
