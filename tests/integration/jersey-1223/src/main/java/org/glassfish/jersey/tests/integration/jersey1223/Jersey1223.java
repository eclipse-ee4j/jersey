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

package org.glassfish.jersey.tests.integration.jersey1223;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

/**
 * @author Michal Gajdos
 */
public class Jersey1223 extends Application {

    @Path(value = "/ContentType")
    public static class ContentTypeResource {

        @POST
        @Produces(value = "text/plain")
        @SuppressWarnings({"UnusedParameters", "JavaDoc"})
        public void postTest(final String str) {
            // Ignore to generate response 204 - NoContent.
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.<Class<?>>singleton(ContentTypeResource.class);
    }
}
