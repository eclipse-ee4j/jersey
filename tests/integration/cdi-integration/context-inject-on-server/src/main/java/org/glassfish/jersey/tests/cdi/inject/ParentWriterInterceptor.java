/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.net.URI;

public interface ParentWriterInterceptor extends ParentChecker, WriterInterceptor {

    static final String STATUS = "status";

    @Override
    default void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean injected = false;

        if (context.getProperty(STATUS) == null) {
            if (getRequestUri().toASCIIString().contains("injected")) {
                injected = checkInjected(stringBuilder);
            }

            if (getRequestUri().toASCIIString().contains("contexted")) {
                injected = checkContexted(stringBuilder);
            }

            if (injected) {
                context.setEntity(context.getEntity().toString().replace("All", "Everything"));
            } else {
                stringBuilder.insert(0, "InjectWriterInterceptor: ");
                context.setEntity(stringBuilder.toString());
            }
        }
        context.proceed();
    }

    URI getRequestUri();
}
