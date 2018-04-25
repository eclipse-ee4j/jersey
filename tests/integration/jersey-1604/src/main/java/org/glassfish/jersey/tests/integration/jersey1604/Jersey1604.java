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

package org.glassfish.jersey.tests.integration.jersey1604;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Application class with test resource that sets {@code Connection} header to {@code close}.
 *
 * @author Michal Gajdos
 */
public class Jersey1604 extends Application {

    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(TestResource.class);
    }

    @Path("/")
    public static class TestResource {

        @DELETE
        public Response delete() {
            return Response
                    .ok()
                    .type(MediaType.TEXT_PLAIN_TYPE)
                    .header("Connection", "close")
                    .build();
        }
    }
}
