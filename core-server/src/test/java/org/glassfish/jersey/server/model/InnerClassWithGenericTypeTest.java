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

package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.InnerClassWithGenericTypeTest
 *
 * @author Paul Sandoz
 */
public class InnerClassWithGenericTypeTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/")
    public static class RootResource {

        @Path("sub")
        public SubResource getSub() {
            return new SubResource(new ArrayList<String>(), new HashSet<Integer>());
        }

        public class SubResource extends RootResource {

            public SubResource(List<String> list, Set<Integer> s) {
            }

            @GET
            public String get() {
                return "sub";
            }
        }
    }

    @Test
    public void testInnerClass() throws Exception {
        app = createApplication(RootResource.class);

        assertEquals("sub", app.apply(RequestContextBuilder.from("/sub", "GET").build()).get().getEntity());
    }
}
