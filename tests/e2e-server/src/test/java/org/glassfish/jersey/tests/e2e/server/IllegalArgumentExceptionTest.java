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

package org.glassfish.jersey.tests.e2e.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IllegalArgumentExceptionTest extends JerseyTest {

    private static final String PARAM_NAME = "paramName";

    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class, TestParamProvider.class);
    }

    @Path("test")
    public static class TestResource {
        @GET
        @Path("1")
        public String get1(@QueryParam(PARAM_NAME) CustomObj value) {
            return "ok";
        }
        @GET
        @Path("2")
        public String get2(@NotNull @QueryParam(PARAM_NAME) String value) {
            return "ok";
        }
        @GET
        @Path("3")
        public String get3(@NotNull @DefaultValue("get3") @QueryParam(PARAM_NAME) CustomObj value) {
            return value.value;
        }
        @GET
        @Path("4")
        public String get4(@HeaderParam(PARAM_NAME) CustomObj header) {
            return "ok";
        }
    }

    @Test
    public void illegalArgumentExceptionWith404() {
        Response response = target().path("test/1").queryParam(PARAM_NAME, 1).request().get();
        assertEquals(404, response.getStatus());
    }

    @Test
    public void validationExceptionWith400() {
        Response response = target().path("test/2").request().get();
        assertEquals(400, response.getStatus());
    }

    @Test
    public void with200() {
        Response response = target().path("test/3").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("get3", response.readEntity(String.class));
    }

    @Test
    public void validationExceptionHeaderWith400() {
        Response response = target().path("test/4").request().header(PARAM_NAME, "1").get();
        assertEquals(400, response.getStatus());
    }

    private static class CustomObj {
        private String value;
    }

    public static class TestParamProvider implements ParamConverterProvider {
        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            return (ParamConverter<T>) new ParamConverter<CustomObj>() {
                @Override
                public CustomObj fromString(String value) {
                    if ("1".equals(value)) {
                        throw new IllegalArgumentException("test exception");
                    } else if (value != null) {
                        CustomObj obj = new CustomObj();
                        obj.value = value;
                        return obj;
                    } else {
                        return null;
                    }
                }
                @Override
                public String toString(CustomObj value) {
                    return null;
                }
            };
        }
    }
}
