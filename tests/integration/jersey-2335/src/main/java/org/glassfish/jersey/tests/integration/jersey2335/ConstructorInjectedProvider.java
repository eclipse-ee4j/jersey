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

package org.glassfish.jersey.tests.integration.jersey2335;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.glassfish.jersey.message.MessageUtils;

/**
 * Constructor injected provider to prove that provider registered via meta-inf/services
 * mechanism gets constructed via HK2 and so the constructor parameters are properly injected.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Provider
@Produces("text/ctor-injected")
public class ConstructorInjectedProvider implements MessageBodyWriter<String> {

    Providers providers;

    public ConstructorInjectedProvider(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return type == String.class;
    }

    @Override
    public long getSize(final String t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final String t, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        final MessageBodyWriter<String> plainTextWriter =
                providers.getMessageBodyWriter(String.class, genericType, annotations, MediaType.TEXT_PLAIN_TYPE);
        entityStream.write("via ctor injected provider:".getBytes(MessageUtils.getCharset(mediaType)));
        plainTextWriter.writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
