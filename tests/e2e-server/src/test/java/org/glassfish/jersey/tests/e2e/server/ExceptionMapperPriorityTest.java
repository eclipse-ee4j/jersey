/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ExceptionMapperPriorityTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ExceptionMapperPriorityResource.class,
                                  MyFirstExceptionMapper.class,
                                  MySecondExceptionMapper.class,
                                  MyThirdExceptionMapper.class);
    }

    @Test
    public void priorityTest() {
        String response = target().request().get(String.class);

        assertThat(response, is(MySecondExceptionMapper.class.getName()));
    }

    @Path("/")
    public static class ExceptionMapperPriorityResource {

        @GET
        public String get() throws MyException {
            throw new MyException();
        }
    }

    public static class MyException extends Exception {

    }

    @Provider
    @Priority(300)
    public static class MyFirstExceptionMapper implements ExceptionMapper<MyException> {

        @Override
        public Response toResponse(MyException exception) {
            return Response.ok(MyFirstExceptionMapper.class.getName()).build();
        }
    }

    @Provider
    @Priority(100)
    public static class MySecondExceptionMapper implements ExceptionMapper<MyException> {

        @Override
        public Response toResponse(MyException exception) {
            return Response.ok(MySecondExceptionMapper.class.getName()).build();
        }
    }

    @Provider
    @Priority(200)
    public static class MyThirdExceptionMapper implements ExceptionMapper<MyException> {

        @Override
        public Response toResponse(MyException exception) {
            return Response.ok(MyThirdExceptionMapper.class.getName()).build();
        }
    }
}
