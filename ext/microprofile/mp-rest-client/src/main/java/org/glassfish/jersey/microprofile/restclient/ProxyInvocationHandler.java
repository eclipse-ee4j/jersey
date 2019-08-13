/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Invocation handler for interface proxy.
 *
 * @author David Kral
 * @author Tomas Langer
 */
class ProxyInvocationHandler implements InvocationHandler {
    private final Client client;
    private final WebTarget target;
    private final RestClientModel restClientModel;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // top level
    ProxyInvocationHandler(Client client,
                           WebTarget target,
                           RestClientModel restClientModel) {

        this.client = client;
        this.target = target;
        this.restClientModel = restClientModel;
    }

    // used for sub-resources
    ProxyInvocationHandler(WebTarget target,
                           RestClientModel restClientModel) {
        this(null, target, restClientModel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName().equals("toString") && (args == null || args.length == 0)) {
            return restClientModel.toString();
        }
        if (method.getName().equals("close") && (args == null || args.length == 0)) {
            closed.set(true);
            if (null != client) {
                client.close();
            }
            return null;
        }

        if (closed.get()) {
            throw new IllegalStateException("Attempting to invoke a method on a closed client.");
        }
        return restClientModel.invokeMethod(target, method, args);
    }

}
