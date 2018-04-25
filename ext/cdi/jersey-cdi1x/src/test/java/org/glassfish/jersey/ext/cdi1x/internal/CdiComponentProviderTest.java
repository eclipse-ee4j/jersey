/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link CdiComponentProvider}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class CdiComponentProviderTest {

    public static class MyMessageBodyReader implements MessageBodyReader {

        @Override
        public boolean isReadable(final Class type,
                                  final Type genericType,
                                  final Annotation[] annotations,
                                  final MediaType mediaType) {
            return true;
        }

        @Override
        public Object readFrom(final Class type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType,
                               final MultivaluedMap httpHeaders,
                               final InputStream entityStream) throws IOException, WebApplicationException {
            return new Object();
        }
    }

    public static class MyOtherMessageBodyReader extends MyMessageBodyReader {
    }

    public static class MyPojo {
    }

    public static class LocatorSubResource {

        @Path("/")
        public Object locator() {
            return this;
        }
    }

    @Path("/")
    public static class ResourceMethodResource {

        @GET
        public Object get() {
            return this;
        }
    }

    public static class ResourceMethodSubResource {

        @GET
        public Object get() {
            return this;
        }
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod(HttpMethod.DELETE)
    public @interface BINGO {
    }

    public static class CustomResourceMethodSubResource {

        @BINGO
        public Object get() {
            return this;
        }
    }

    /**
     * Test provider detection.
     */
    @Test
    public void testProviders() {
        final CdiComponentProvider provider = new CdiComponentProvider();
        assertFalse(provider.isJaxRsComponentType(MyPojo.class));
        assertTrue(provider.isJaxRsComponentType(MyMessageBodyReader.class));
        assertTrue(provider.isJaxRsComponentType(MyOtherMessageBodyReader.class));
    }

    /**
     * Test sub-resource detection.
     */
    @Test
    public void testResources() {
        final CdiComponentProvider provider = new CdiComponentProvider();
        assertTrue(provider.isJaxRsComponentType(LocatorSubResource.class));
        assertTrue(provider.isJaxRsComponentType(ResourceMethodResource.class));
        assertTrue(provider.isJaxRsComponentType(ResourceMethodSubResource.class));
        assertTrue(provider.isJaxRsComponentType(CustomResourceMethodSubResource.class));
    }
}
