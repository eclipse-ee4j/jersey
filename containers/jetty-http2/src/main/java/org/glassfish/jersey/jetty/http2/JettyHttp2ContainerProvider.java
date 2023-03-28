/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.http2;

import org.eclipse.jetty.server.Handler;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.jetty.JettyHttpContainerProvider;
import org.glassfish.jersey.server.spi.ContainerProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

public final class JettyHttp2ContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(final Class<T> type, final Application application) throws ProcessingException {
        if (Handler.class == type || JettyHttpContainer.class == type) {
            return type.cast(new JettyHttpContainerProvider().createContainer(JettyHttpContainer.class,
                    application));
        }
        return null;
    }
}

