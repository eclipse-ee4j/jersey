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

package org.glassfish.jersey.server.internal.inject;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("unchecked")

public class EncodedParamsTest extends AbstractTest {

    @Path("/{u}")
    public static class EncodedOnPostClass {

        public EncodedOnPostClass(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m,
                @FormParam("f") String f) {
            assertEquals(" u", u);
            assertEquals(" q", q);
            assertEquals(" m", m);
            assertEquals(":f", f);
        }

        @POST
        @Encoded
        public String doPost(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m,
                @FormParam("f") String f) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            assertEquals("%3Af", f);
            return "content";
        }

        @POST
        @Path("combined")
        public String doPostCombined(
                @Encoded @FormParam("f") String f,
                @FormParam("f2") String f2) {
            assertEquals("%3Af", f);
            assertEquals(":f2", f2);
            return "content";
        }
    }

    @Test
    public void testEncodedOnPostClass() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnPostClass.class);
        Form form = new Form();
        form.param("f", ":f");
        RequestContextBuilder requestBuilder = RequestContextBuilder.from("/%20u;m=%20m?q=%20q",
                "POST").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).entity(form);
        assertEquals("content", apply(requestBuilder.build()).getEntity());
    }

    @Test
    public void testCombinedEncodedOnPostClass() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnPostClass.class);
        Form form = new Form();
        form.param("f", ":f");
        form.param("f2", ":f2");
        RequestContextBuilder requestBuilder = RequestContextBuilder.from("/%20u/combined;m=%20m?q=%20q",
                "POST").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).entity(form);
        assertEquals("content", apply(requestBuilder.build()).getEntity());
    }

    @Encoded
    @Path("/{u}")
    public static class EncodedOnClass {

        public EncodedOnClass(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }

        @GET
        public String doGet(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }

    @Test
    public void testEncodedOnClass() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnClass.class);

        _test("/%20u;m=%20m?q=%20q");
    }


    @Path("/{u}")
    public static class EncodedOnAccessibleObject {

        @Encoded
        public EncodedOnAccessibleObject(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }

        @Encoded
        @GET
        public String doGet(
                @PathParam("u") String u,
                @QueryParam("q") String q,
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }

    @Test
    public void testEncodedOnAccessibleObject() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnAccessibleObject.class);

        _test("/%20u;m=%20m?q=%20q");
    }

    @Path("/{u}")
    public static class EncodedOnParameters {

        public EncodedOnParameters(
                @Encoded @PathParam("u") String u,
                @Encoded @QueryParam("q") String q,
                @Encoded @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }

        @GET
        public String doGet(
                @Encoded @PathParam("u") String u,
                @Encoded @QueryParam("q") String q,
                @Encoded @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }

    @Test
    public void testEncodedOnParameters() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnParameters.class);

        _test("/%20u;m=%20m?q=%20q");
    }

    @Path("/{u}")
    public static class MixedEncodedOnParameters {

        public MixedEncodedOnParameters(
                @PathParam("u") String du,
                @QueryParam("q") String dq,
                @MatrixParam("m") String dm,
                @Encoded @PathParam("u") String eu,
                @Encoded @QueryParam("q") String eq,
                @Encoded @MatrixParam("m") String em) {
            assertEquals(" u", du);
            assertEquals(" q", dq);
            assertEquals(" m", dm);
            assertEquals("%20u", eu);
            assertEquals("%20q", eq);
            assertEquals("%20m", em);
        }

        @GET
        public String doGet(
                @PathParam("u") String du,
                @QueryParam("q") String dq,
                @MatrixParam("m") String dm,
                @Encoded @PathParam("u") String eu,
                @Encoded @QueryParam("q") String eq,
                @Encoded @MatrixParam("m") String em) {
            assertEquals(" u", du);
            assertEquals(" q", dq);
            assertEquals(" m", dm);
            assertEquals("%20u", eu);
            assertEquals("%20q", eq);
            assertEquals("%20m", em);
            return "content";
        }
    }

    @Test
    public void testMixedEncodedOnParameters() throws ExecutionException, InterruptedException {
        initiateWebApplication(MixedEncodedOnParameters.class);

        _test("/%20u;m=%20m?q=%20q");
    }

    @Path("/")
    public static class EncodedOnFormParameters {

        public EncodedOnFormParameters(
                @Encoded @FormParam("u") String ue,
                @FormParam("u") String u) {

            assertThat(ue, is("%C5%A1"));
            assertThat(ue, is(not("\u0161")));

            assertThat(u, is(not("%C5%A1")));
            assertThat(u, is("\u0161"));
        }

        @POST
        public String doPost(
                @Encoded @FormParam("u") String ue,
                @FormParam("u") String u) {

            assertThat(ue, is("%C5%A1"));
            assertThat(ue, is(not("\u0161")));

            assertThat(u, is(not("%C5%A1")));
            assertThat(u, is("\u0161"));

            return "content";
        }
    }

    @Test
    public void testEncodedOnFormParameters() throws ExecutionException, InterruptedException {
        initiateWebApplication(EncodedOnFormParameters.class);

        final RequestContextBuilder requestBuilder = RequestContextBuilder.from("/", "POST")
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .entity(new Form("u", "\u0161"));

        apply(requestBuilder.build());
    }
}
