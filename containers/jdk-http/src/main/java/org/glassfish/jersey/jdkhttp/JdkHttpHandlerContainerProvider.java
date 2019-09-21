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

package org.glassfish.jersey.jdkhttp;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.spi.ContainerProvider;

import com.sun.net.httpserver.HttpHandler;

/**
 * Container provider for containers based on lightweight Java SE HTTP Server's {@link HttpHandler}.
 *
 * @author Miroslav Fuksa
 */
public final class JdkHttpHandlerContainerProvider implements ContainerProvider {

    @Override
    public <T> T createContainer(Class<T> type, Application application) throws ProcessingException {
        if (type != HttpHandler.class && type != JdkHttpHandlerContainer.class) {
            return null;
        }
        return type.cast(new JdkHttpHandlerContainer(application));
    }
}
