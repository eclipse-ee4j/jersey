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

package org.glassfish.jersey.media.htmljson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.assertEquals;

/**
 * Abstract entity type tester base class.
 *
 * @author Paul Sandoz
 * @author Martin Matula
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractTypeTester extends JerseyTest {

    protected static byte[] requestEntity;

    public abstract static class AResource<T> {

        @POST
        public T post(T t) {
            return t;
        }
    }

    public static class RequestEntityInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
            OutputStream original = writerInterceptorContext.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writerInterceptorContext.setOutputStream(baos);
            writerInterceptorContext.proceed();
            requestEntity = baos.toByteArray();
            original.write(requestEntity);
        }
    }

    /**
     * Looks for all resources and providers declared as inner classes of the subclass of this class
     * and adds them to the returned ResourceConfig (unless constrained to client side).
     *
     * @return ResourceConfig instance
     */
    @Override
    protected Application configure() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();

        for (Class<?> cls : getClass().getDeclaredClasses()) {
            if (cls.getAnnotation(Path.class) != null) {
                classes.add(cls);
            } else if (cls.getAnnotation(Provider.class) != null) {
                final ConstrainedTo constrainedTo = cls.getAnnotation(ConstrainedTo.class);
                if (constrainedTo == null || constrainedTo.value() == RuntimeType.SERVER) {
                    classes.add(cls);
                }
            }
        }

        return new ResourceConfig(classes);
    }

    /**
     * Looks for all providers declared as inner classes of the subclass of this class
     * and adds them to the client configuration (unless constrained to server side).
     */
    @Override
    protected void configureClient(ClientConfig config) {
        config.register(RequestEntityInterceptor.class);

        for (Class<?> cls : getClass().getDeclaredClasses()) {
            if (cls.getAnnotation(Provider.class) != null) {
                final ConstrainedTo constrainedTo = cls.getAnnotation(ConstrainedTo.class);
                if (constrainedTo == null || constrainedTo.value() == RuntimeType.CLIENT) {
                    config.register(cls);
                }
            }
        }
    }

    protected <T> void _test(T in, Class resource) {
        _test(in, resource, true);
    }

    protected <T> void _test(T in, Class resource, MediaType m) {
        _test(in, resource, m, true);
    }

    protected <T> void _test(T in, Class resource, boolean verify) {
        _test(in, resource, MediaType.TEXT_PLAIN_TYPE, verify);
    }

    protected <T> void _test(T in, Class resource, MediaType m, boolean verify) {
        WebTarget target = target(resource.getSimpleName());
        Response response = target.request().post(Entity.entity(in, m));

        byte[] inBytes = requestEntity;
        byte[] outBytes = getEntityAsByteArray(response);

        if (verify) {
            _verify(inBytes, outBytes);
        }
    }

    protected static void _verify(byte[] in, byte[] out) {
        assertEquals(in.length, out.length);
        for (int i = 0; i < in.length; i++) {
            if (in[i] != out[i]) {
                assertEquals("Index: " + i, in[i], out[i]);
            }
        }
    }

    protected static byte[] getEntityAsByteArray(Response r) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ReaderWriter.writeTo(r.readEntity(InputStream.class), baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}
