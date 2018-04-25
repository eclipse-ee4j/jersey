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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey 1: jersey-tests:com.sun.jersey.impl.resource.ConsumeProduceWildcardTest.java
 *
 * @author Paul Sandoz
 */
public class ConsumeProduceWildcardTest {

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/{arg1}/{arg2}")
    @Consumes("text/*")
    public static class ConsumeWildCardBean {

        @Context
        HttpHeaders headers;

        @POST
        public String doPostHtml() {
            assertEquals("text/html", headers.getRequestHeaders().getFirst("Content-Type"));
            return "HTML";
        }

        @POST
        @Consumes("text/xhtml")
        public String doPostXHtml() {
            assertEquals("text/xhtml", headers.getRequestHeaders().getFirst("Content-Type"));
            return "XHTML";
        }
    }

    @Test
    public void testConsumeWildCardBean() throws Exception {
        ApplicationHandler app = createApplication(ConsumeWildCardBean.class);

        assertEquals("HTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/html").build()).get().getEntity());
        assertEquals("XHTML",
                app.apply(RequestContextBuilder.from("/a/b", "POST").entity("").type("text/xhtml").build()).get().getEntity());
    }
}
