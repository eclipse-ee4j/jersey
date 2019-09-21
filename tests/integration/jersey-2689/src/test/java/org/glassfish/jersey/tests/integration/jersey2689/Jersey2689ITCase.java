/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2689;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * Tests for JERSEY-2689: Problem with validation errors on primitive type arrays.
 * <p/> There is a bug when a validation fails for a primitive data array. Eg a NotNull failed validation on a byte[] causes the code to throw a ClassCastException. The problem is caused by ValidationHelper.getViolationInvalidValue(Object invalidValue) It tries to cast any array to a Object[] A byte[] parameter would generate a ClassCastException.*
 * @author Oscar Guindzberg (oscar.guindzberg at gmail.com)
 */
public class Jersey2689ITCase extends JerseyTest {


    @Override
    protected ResourceConfig configure() {
        return new Jersey2689();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Checks if a thread gets stuck when an {@code IOException} is thrown from the {@code
     * MessageBodyWriter#writeTo}.
     */
    @Test
    public void testByteArray() throws Exception {
        // Executor.
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Response> responseFuture = executor.submit(new Callable<Response>() {

            @Override
            public Response call() throws Exception {
                SampleBean bean = new SampleBean();
                bean.setArray(new byte[]{});

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
                provider.setMapper(mapper);
                client().register(provider);

                return target().path("post-bean").request().post(Entity.entity(bean, MediaType.APPLICATION_JSON));
            }

        });

        executor.shutdown();
        final boolean inTime = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        // Asserts.
        assertTrue(inTime);

        // Response.
        final Response response = responseFuture.get();

        //Make sure we get a 400 error and not a 500 error
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusInfo().getStatusCode());

    }


}
