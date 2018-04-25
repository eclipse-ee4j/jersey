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

package org.glassfish.jersey.tests.integration.tracing;

import java.io.IOException;
import java.util.Date;
import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Provider
@Priority(45)
public class WriterInterceptor45 implements WriterInterceptor {
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        //System.out.println("*** WriterInterceptor45.aroundWriteTo: BEFORE");
        context.getHeaders().putSingle(WriterInterceptor45.class.getSimpleName(), new Date());
        context.proceed();
        try {
            Thread.sleep(42);
        } catch (InterruptedException e) {
        }
        //System.out.println("*** WriterInterceptor45.aroundWriteTo: AFTER");
    }
}
