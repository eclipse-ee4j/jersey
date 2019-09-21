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

package org.glassfish.jersey.tests.integration.jersey2255;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AnnotationLiteral;
import org.glassfish.jersey.message.filtering.EntityFiltering;

/**
 * Test resource.
 *
 * @author Eric Miles (emilesvt at gmail.com)
 */
@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class Issue2255Resource {

    public static class A {

        public A() {
        }

        public A(String fieldA1) {
            this.fieldA1 = fieldA1;
        }

        private String fieldA1;

        @Detailed
        public String getFieldA1() {
            return fieldA1;
        }

        public void setFieldA1(final String fieldA1) {
            this.fieldA1 = fieldA1;
        }
    }

    public static class B extends A {

        public B() {
        }

        public B(String fieldA1, String fieldB1) {
            super(fieldA1);
            this.fieldB1 = fieldB1;
        }

        private String fieldB1;

        public String getFieldB1() {
            return fieldB1;
        }

        public void setFieldB1(final String fieldB1) {
            this.fieldB1 = fieldB1;
        }
    }

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @EntityFiltering
    public static @interface Detailed {

        /**
         * Factory class for creating instances of {@code ProjectDetailedView} annotation.
         */
        public static class Factory
                extends AnnotationLiteral<Detailed>
                implements Detailed {

            private Factory() {
            }

            public static Detailed get() {
                return new Factory();
            }
        }
    }

    @Path("A")
    @GET
    public Response getA(@QueryParam("detailed") final boolean isDetailed) {
        return Response
                .ok()
                .entity(new A("fieldA1Value"), isDetailed ? new Annotation[] {Detailed.Factory.get()} : new Annotation[0])
                .build();
    }

    @Path("B")
    @GET
    public Response getB(@QueryParam("detailed") final boolean isDetailed) {
        return Response
                .ok()
                .entity(new B("fieldA1Value", "fieldB1Value"),
                        isDetailed ? new Annotation[] {Detailed.Factory.get()} : new Annotation[0])
                .build();
    }

}
