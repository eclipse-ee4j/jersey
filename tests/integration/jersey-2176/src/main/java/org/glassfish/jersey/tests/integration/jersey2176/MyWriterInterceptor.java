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

package org.glassfish.jersey.tests.integration.jersey2176;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.message.MessageUtils;


/**
 * This just set new context output stream and test a clone method on set output stream instance is called.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class MyWriterInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException {
        final boolean fail = context.getHeaders().containsKey(Issue2176ReproducerResource.X_FAIL_HEADER);
        final boolean responseEntity = context.getHeaders().containsKey(Issue2176ReproducerResource.X_RESPONSE_ENTITY_HEADER);

        if (responseEntity) {
            context.setOutputStream(
                    new MyOutputStream(context.getOutputStream(), MessageUtils.getCharset(context.getMediaType())));
        }
        context.proceed();
        if (fail) {
            throw new IllegalStateException("From MyWriterInterceptor");
        }
    }

    private static class MyOutputStream extends OutputStream {
        private final OutputStream delegate;
        final Charset charset;
        private final ByteArrayOutputStream localStream;

        private MyOutputStream(final OutputStream delegate, final Charset charset) throws IOException {
            this.delegate = delegate;
            this.charset = charset;

            this.localStream = new ByteArrayOutputStream();
            localStream.write("[INTERCEPTOR]".getBytes(charset));
        }

        @Override
        public void write(final int b) throws IOException {
            localStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            delegate.write(localStream.toByteArray());
            localStream.reset();
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            localStream.write("[/INTERCEPTOR]".getBytes(charset));

            delegate.write(localStream.toByteArray());

            delegate.close();
            localStream.close();
        }
    }

}
