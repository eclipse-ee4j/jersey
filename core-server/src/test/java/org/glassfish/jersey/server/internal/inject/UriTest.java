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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.server.ClientBinding;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.Uri;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class UriTest extends AbstractTest {

    @Path("test")
    public static class Resource1 {

        @Uri("http://oracle.com")
        WebTarget webTarget1;

        @GET
        @Path("1")
        public String doGet1() {
            return webTarget1.getUri().toString();
        }

        @GET
        @Path("2")
        public String doGet2(@Uri("http://oracle.com") WebTarget webTarget2) {
            return webTarget2.getUri().toString();
        }
    }

    @Path("test")
    public static class Resource2 {

        @Uri("http://oracle.com/{param}")
        WebTarget webTarget1;

        @GET
        @Path("1")
        public String doGet1() {
            return webTarget1.getUri() == null ? "null" : webTarget1.getUri().toString();
        }

        @GET
        @Path("{param}")
        public String doGet2(@Uri("http://oracle.com/{param}") WebTarget webTarget2) {
            return webTarget2.getUri().toString();
        }
    }

    @Path("test")
    public static class Resource3 {

        @Uri("{param}")
        WebTarget webTarget1;

        @GET
        @Path("1")
        public String doGet1() {
            return webTarget1.getUri() == null ? "null" : webTarget1.getUri().toString();
        }

        @GET
        @Path("{param}")
        public String doGet2(@Uri("{param}") WebTarget webTarget2) {
            return webTarget2.getUri().toString();
        }
    }


    @ClientBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    public static @interface Managed {

    }

    @Path("test")
    public static class Resource4 {

        @Uri("http://oracle.com")
        @Managed
        WebTarget webTarget1;

        @GET
        @Path("1")
        public String doGet1() {
            return (String) webTarget1.getConfiguration().getProperties().get("test-property");
        }

        @GET
        @Path("2")
        public String doGet2(@Uri("http://oracle.com") @Managed WebTarget webTarget2) {
            return (String) webTarget2.getConfiguration().getProperties().get("test-property");
        }

        @GET
        @Path("3")
        public String doGet3(@Uri("relative") @Managed WebTarget relativeTarget) {
            return relativeTarget.getUri().toString();
        }
    }

    @Test
    public void testGet1() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource1.class);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/1", "GET")
                        .build()
        );

        assertEquals("http://oracle.com", response.getEntity());
    }

    @Test
    public void testGet2() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource1.class);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/2", "GET")
                        .build()
        );

        assertEquals("http://oracle.com", response.getEntity());
    }


    @Test
    public void testGetParam1() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource2.class);

        try {
            apply(
                    RequestContextBuilder.from("/test/1", "GET").build()
            );
        } catch (ExecutionException ex) {
            // ISE thrown from WebTarget
            assertThat(ex.getCause(), instanceOf(IllegalStateException.class));
            // IAE thrown from UriBuilder - unresolved template parameter value
            assertThat(ex.getCause().getCause(), instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testGetParam2() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource2.class);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/parameter", "GET")
                        .build()
        );

        assertEquals("http://oracle.com/parameter", response.getEntity());
    }


    @Test
    public void testGetRelative1() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource3.class);

        try {
            apply(
                    RequestContextBuilder.from("/test/1", "GET").build()
            );
        } catch (ExecutionException ex) {
            // ISE thrown from WebTarget
            assertThat(ex.getCause(), instanceOf(IllegalStateException.class));
            // IAE thrown from UriBuilder - unresolved template parameter value
            assertThat(ex.getCause().getCause(), instanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testGetRelative2() throws ExecutionException, InterruptedException {
        initiateWebApplication(Resource3.class);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/parameter", "GET")
                        .build()
        );

        assertEquals("/parameter", response.getEntity());
    }

    @Test
    public void testManagedClientInjection1() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource4.class);
        // TODO introduce new ResourceConfig.setClientProperty(Class<? extends Annotation>, String name, Object value) helper method
        resourceConfig.property(Managed.class.getName() + ".property.test-property", "test-value");
        initiateWebApplication(resourceConfig);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/1", "GET")
                        .build()
        );

        assertEquals("test-value", response.getEntity());
    }

    @Test
    public void testManagedClientInjection2() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource4.class);
        resourceConfig.property(Managed.class.getName() + ".property.test-property", "test-value");
        initiateWebApplication(resourceConfig);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/2", "GET")
                        .build()
        );

        assertEquals("test-value", response.getEntity());
    }

    @Test
    public void testManagedClientInjection3() throws ExecutionException, InterruptedException {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource4.class);
        resourceConfig.property(Managed.class.getName() + ".property.test-property", "test-value");
        resourceConfig.property(Managed.class.getName() + ".baseUri", "http://oracle.com");
        initiateWebApplication(resourceConfig);

        final ContainerResponse response = apply(
                RequestContextBuilder.from("/test/3", "GET")
                        .build()
        );

        assertEquals("http://oracle.com/relative", response.getEntity());
    }

    @Path("test")
    public static class Resource5 {

        @Uri("http://oracle.com/{template}")
        WebTarget webTarget1;

        @GET
        @Path("1")
        public String doGet1() {
            return webTarget1.resolveTemplate("template", "foo").getUri().toString();
        }

        @GET
        @Path("2")
        public String doGet2(@Uri("http://oracle.com/{template}") WebTarget webTarget2) {
            return webTarget2.resolveTemplate("template", "bar").getUri().toString();
        }
    }

    @Test
    public void testResolveTemplateInFieldManagedClient() throws Exception {
        initiateWebApplication(Resource5.class);
        final ContainerResponse response = apply(RequestContextBuilder.from("/test/1", "GET").build());

        assertThat(response.getEntity().toString(), equalTo("http://oracle.com/foo"));
    }

    @Test
    public void testResolveTemplateInParamManagedClient() throws Exception {
        initiateWebApplication(Resource5.class);
        final ContainerResponse response = apply(RequestContextBuilder.from("/test/2", "GET").build());

        assertThat(response.getEntity().toString(), equalTo("http://oracle.com/bar"));
    }
}
