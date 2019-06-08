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

package org.glassfish.jersey.tests.e2e.client;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.hamcrest.Matchers.is;

public class ClientExceptionMapperTest extends JerseyTest {

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

        @Override
        public Response toResponse(NotFoundException exception) {
            return Response.ok("404").build();
        }
    }

    @Path("/test")
    public static class TestResource {
        @GET
        public String ok() {
            return "ok";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig().register(TestResource.class);
    }

    @Test
    public void test404get() {
        String entity = target().path("nowhere")
                .register(NotFoundExceptionMapper.class)
                .request().get(String.class);
        Assert.assertThat(entity, is("404"));
    }

    @Test
    public void test404getGenericType() {
        String entity = target().path("nowhere")
                .register(NotFoundExceptionMapper.class)
                .request().get(new GenericType<String>(String.class){});
        Assert.assertThat(entity, is("404"));
    }

    @Test
    public void test404post() {
        String entity = target().path("nowhere")
                .register(NotFoundExceptionMapper.class)
                .request().post(Entity.entity("something", MediaType.TEXT_PLAIN_TYPE), String.class);
        Assert.assertThat(entity, is("404"));
    }
}
