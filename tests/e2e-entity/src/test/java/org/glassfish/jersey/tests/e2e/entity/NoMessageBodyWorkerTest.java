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

package org.glassfish.jersey.tests.e2e.entity;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 */
public class NoMessageBodyWorkerTest extends AbstractTypeTester {

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = (ResourceConfig) super.configure();
        resourceConfig.property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
        return resourceConfig;
    }

    @Path("nobodyreader")
    public static class NoMessageBodyReaderResource {

        @POST
        public void post(NoMessageBodyReaderResource t) {
        }
    }

    @Test
    public void testNoMessageBodyReaderResource() {
        Response r = target("nobodyreader").request().post(Entity.text("a"));
        assertEquals(415, r.getStatus());
    }

    @Path("nobodywriter")
    public static class NoMessageBodyWriterResource {

        @GET
        public NoMessageBodyWriterResource get() {
            return new NoMessageBodyWriterResource();
        }
    }

    @Test
    public void testNoMessageBodyWriterResource() {
        Response r = target("nobodywriter").request().get();
        assertEquals(500, r.getStatus());
    }
}
