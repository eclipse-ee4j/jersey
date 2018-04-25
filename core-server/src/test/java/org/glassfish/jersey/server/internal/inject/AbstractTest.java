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

package org.glassfish.jersey.server.internal.inject;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Request;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import static org.junit.Assert.assertEquals;

/**
 * Class used for {@link ApplicationHandler} initialization and for executing {@link Request}s.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public abstract class AbstractTest {

    private ApplicationHandler app;

    protected void initiateWebApplication(Class<?>... classes) {
        app = new ApplicationHandler(new ResourceConfig(classes));
    }

    protected void initiateWebApplication(ResourceConfig resourceConfig) {
        app = new ApplicationHandler(resourceConfig);
    }

    protected ContainerResponse apply(ContainerRequest request)
            throws ExecutionException, InterruptedException {

        return app.apply(request).get();
    }

    protected ContainerResponse getResponseContext(String requestUri, Cookie... cookies)
            throws ExecutionException, InterruptedException {

        return getResponseContext(requestUri, null, cookies);
    }

    protected ContainerResponse getResponseContext(String requestUri, String accept, Cookie... cookies)
            throws ExecutionException, InterruptedException {

        RequestContextBuilder requestBuilder = RequestContextBuilder.from(requestUri, "GET");
        if (accept != null) {
            requestBuilder = requestBuilder.accept(accept);
        }
        requestBuilder = requestBuilder.cookies(cookies);

        return apply(requestBuilder.build());
    }

    protected void _test(String requestUri, String accept, Cookie... cookies)
            throws ExecutionException, InterruptedException {

        assertEquals("content", getResponseContext(requestUri, accept, cookies).getEntity());
    }

    protected void _test(String requestUri, Cookie... cookies) throws ExecutionException, InterruptedException {
        _test(requestUri, null, cookies);
    }

    public ApplicationHandler app() {
        return app;
    }
}
