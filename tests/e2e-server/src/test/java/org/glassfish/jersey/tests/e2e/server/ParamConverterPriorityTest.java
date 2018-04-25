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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import javax.annotation.Priority;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.typeCompatibleWith;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ParamConverterPriorityTest extends JerseyTest {

    private static final List<Class<? extends ParamConverterProvider>> CONVERTER_PROVIDER_CALL_ORDER = new ArrayList<>();

    @Override
    protected Application configure() {
        return new ResourceConfig(ParamConverterPriorityResource.class,
                                  MyParamConverterProvider1.class,
                                  MyParamConverterProvider2.class,
                                  MyParamConverterProvider3.class);
    }

    @Test
    public void test() {
        Response response = target().queryParam("test", "value").request().get();

        assertThat(CONVERTER_PROVIDER_CALL_ORDER.size(), greaterThanOrEqualTo(3));
        assertThat(CONVERTER_PROVIDER_CALL_ORDER.get(0), typeCompatibleWith(MyParamConverterProvider2.class));
        assertThat(CONVERTER_PROVIDER_CALL_ORDER.get(1), typeCompatibleWith(MyParamConverterProvider1.class));
        assertThat(CONVERTER_PROVIDER_CALL_ORDER.get(2), typeCompatibleWith(MyParamConverterProvider3.class));
    }

    @Path("/")
    public static class ParamConverterPriorityResource {

        @GET
        public String get(@QueryParam("test") String param) {
            return param;
        }
    }

    @Priority(200)
    public static class MyParamConverterProvider1 implements ParamConverterProvider {
        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            CONVERTER_PROVIDER_CALL_ORDER.add(this.getClass());
            return null;
        }
    }

    @Priority(100)
    public static class MyParamConverterProvider2 implements ParamConverterProvider {
        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            CONVERTER_PROVIDER_CALL_ORDER.add(this.getClass());
            return null;
        }
    }

    @Priority(300)
    public static class MyParamConverterProvider3 implements ParamConverterProvider {
        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            CONVERTER_PROVIDER_CALL_ORDER.add(this.getClass());
            return null;
        }
    }
}
