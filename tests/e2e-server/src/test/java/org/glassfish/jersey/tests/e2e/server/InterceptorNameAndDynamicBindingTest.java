/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class InterceptorNameAndDynamicBindingTest extends JerseyTest {

    static final String ENTITY = "ENTITY";

    @Override
    protected void configureClient(ClientConfig config) {
        super.configureClient(config);
    }

    abstract static class PrefixAddingReaderInterceptor implements ReaderInterceptor {

        public PrefixAddingReaderInterceptor() {
        }

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            context.setInputStream(
                    new SequenceInputStream(new ByteArrayInputStream(getPrefix().getBytes()), context.getInputStream()));
            return context.proceed();
        }

        abstract String getPrefix();
    }

    abstract static class PrefixAddingWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            context.getOutputStream().write(getPrefix().getBytes());
            context.proceed();
        }

        abstract String getPrefix();
    }

    @NameBinding
    @Retention(RetentionPolicy.RUNTIME)
    static @interface NameBoundReader {
    }

    @NameBoundReader
    @Priority(40)
    static class NameBoundReaderInterceptor extends PrefixAddingReaderInterceptor {

        @Override
        String getPrefix() {
            return "nameBoundReader";
        }
    }

    @Priority(60)
    static class DynamicallyBoundReaderInterceptor extends PrefixAddingReaderInterceptor {

        @Override
        String getPrefix() {
            return "dynamicallyBoundReader";
        }
    }

    @NameBinding
    @Priority(40)
    @Retention(RetentionPolicy.RUNTIME)
    static @interface NameBoundWriter {
    }

    @NameBoundWriter
    public static class NameBoundWriterInterceptor extends PrefixAddingWriterInterceptor {

        @Override
        String getPrefix() {
            return "nameBoundWriter";
        }
    }

    @Priority(20)
    public static class DynamicallyBoundWriterInterceptor extends PrefixAddingWriterInterceptor {

        @Override
        String getPrefix() {
            return "dynamicallyBoundWriter";
        }
    }

    @Path("method")
    public static class MethodBindingResource {

        @Path("dynamicallyBoundWriter")
        @GET
        public String getDynamicallyBoundWriter() {
            return ENTITY;
        }

        @Path("nameBoundWriter")
        @GET
        @NameBoundWriter
        public String getNameBoundWriter() {
            return ENTITY;
        }

        @Path("dynamicallyBoundReader")
        @POST
        public String postDynamicallyBoundReader(String input) {
            return input;
        }

        @Path("nameBoundReader")
        @POST
        @NameBoundReader
        public String postNameBoundReader(String input) {
            return input;
        }
    }

    @Path("class")
    @NameBoundWriter
    public static class ClassBindingResource {

        @Path("nameBoundWriter")
        @GET
        public String getNameBoundWriter() {
            return ENTITY;
        }

        @Path("nameBoundReader")
        @POST
        public String postNameBoundReader(String input) {
            return input;
        }
    }

    @Path("mixed")
    @NameBoundWriter
    public static class MixedBindingResource {

        @Path("nameBoundWriterDynamicReader")
        @POST
        public String postNameBoundWrDynamicallyBoundReader(String input) {
            return input;
        }

        @Path("nameBoundWriterDynamicWriterNameBoundReader")
        @POST
        @NameBoundReader
        public String postNameBoundReWrDynamicallyBoundWriter(String input) {
            return input;
        }
    }

    static final Pattern ReaderMETHOD = Pattern.compile(".*Dynamically.*Reader");
    static final Pattern WriterMETHOD = Pattern.compile(".*Dynamically.*Writer");

    @Override
    protected Application configure() {
        return new ResourceConfig(MethodBindingResource.class, ClassBindingResource.class,
                MixedBindingResource.class, NameBoundReaderInterceptor.class, NameBoundWriterInterceptor.class).registerInstances(
                new DynamicFeature() {

                    @Override
                    public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {
                        if (ReaderMETHOD.matcher(resourceInfo.getResourceMethod().getName()).matches()) {
                            context.register(DynamicallyBoundReaderInterceptor.class);
                        }
                    }
                },
                new DynamicFeature() {

                    @Override
                    public void configure(final ResourceInfo resourceInfo, final FeatureContext context) {
                        if (WriterMETHOD.matcher(resourceInfo.getResourceMethod().getName()).matches()) {
                            context.register(DynamicallyBoundWriterInterceptor.class);
                        }
                    }
                }
        );
    }

    @Test
    public void testNameBoundReaderOnMethod() {
        _testReader("method", "nameBoundReader");
    }

    @Test
    public void testNameBoundWriterOnMethod() {
        _testWriter("method", "nameBoundWriter");
    }

    @Test
    public void testNameBoundReaderOnClass() {
        _testReader("class", "nameBoundReader", "nameBoundWriterENTITY");
    }

    @Test
    public void testNameBoundWriterOnClass() {
        _testWriter("class", "nameBoundWriter");
    }

    @Test
    public void testDynamicallyBoundReaderOnMethod() {
        _testReader("method", "dynamicallyBoundReader");
    }

    @Test
    public void testDynamicallyBoundWriterOnMethod() {
        _testWriter("method", "dynamicallyBoundWriter");
    }

    @Test
    public void testDynamicReaderOnMethodNamedWriterOnClass() {
        _testReader("mixed", "nameBoundWriterDynamicReader", "nameBoundWriterdynamicallyBoundReaderENTITY");
    }

    @Test
    public void testNameBoundWriterDynamicWriterNameBoundReader() {
        _testReader("mixed", "nameBoundWriterDynamicWriterNameBoundReader",
                "dynamicallyBoundWriternameBoundWriternameBoundReaderENTITY");
    }

    private void _testReader(String root, String id) {
        _testReader(root, id, id + ENTITY);
    }

    private void _testReader(String root, String id, String expected) {
        Response r = target(root + "/" + id).request().post(Entity.entity(ENTITY, MediaType.TEXT_PLAIN));
        assertEquals(200, r.getStatus());
        assertEquals(expected, r.readEntity(String.class));
    }

    private void _testWriter(String root, String id) {
        _testWriter(root, id, id + ENTITY);
    }

    private void _testWriter(String root, String id, String expected) {
        Response r = target(root + "/" + id).request().get();
        assertEquals(200, r.getStatus());
        assertEquals(expected, r.readEntity(String.class));
    }
}
