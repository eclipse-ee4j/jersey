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

package org.glassfish.jersey.client;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import java.net.ConnectException;

import static org.hamcrest.Matchers.is;

public class ClientExceptionMapperTest {
    private static final String expectedResponse = "HelloFromExceptionMapper";

    @Test
    public void exceptionMapperNoConstraintToTest() {
        try {
            final String response = ClientBuilder.newClient()
                    .register(new ExceptionMapper<Throwable>() {

                        @Override
                        public Response toResponse(Throwable exception) {
                            return Response.ok(expectedResponse).build();
                        }
                    })
                    .target("http://localhost:9900/doesnotexist").request().get(String.class);
            Assert.fail("ProcessingException has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

    @Test
    public void exceptionMapperConstraintClientToTest() {
        final String response = ClientBuilder.newClient()
                .register(ClientExceptionMapper.class)
                .target("http://localhost:9900/doesnotexist").request().get(String.class);
        Assert.assertThat(response, is(expectedResponse));
    }

    @Test
    public void exceptionMapperConstraintToServerTest() {
        try {
            final String response = ClientBuilder.newClient()
                    .register(ServerExceptionMapper.class)
                    .target("http://localhost:9900/doesnotexist").request().get(String.class);
            Assert.assertThat(response, is(expectedResponse));
            Assert.fail("ProcessingException has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class ClientExceptionMapper implements ExceptionMapper<ConnectException> {
        @Override
        public Response toResponse(ConnectException exception) {
            return Response.ok(expectedResponse).build();
        }
    }

    @ConstrainedTo(RuntimeType.SERVER)
    public static class ServerExceptionMapper implements ExceptionMapper<ConnectException> {
        @Override
        public Response toResponse(ConnectException exception) {
            return Response.ok(expectedResponse).build();
        }
    }
}
