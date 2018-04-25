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

package org.glassfish.jersey.tests.integration.tracing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.MessageUtils;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Provider
@Produces(Utils.APPLICATION_X_JERSEY_TEST)
public class MessageBodyWriterTestFormat implements MessageBodyWriter<Message> {

    boolean serverSide = true;

    public MessageBodyWriterTestFormat() {
    }

    public MessageBodyWriterTestFormat(final boolean serverSide) {
        this.serverSide = serverSide;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return type.isAssignableFrom(Message.class);
    }

    @Override
    public long getSize(final Message message, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Message message, final Class<?> type, final Type genericType, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        if (serverSide) {
            Utils.throwException(message.getText(), this,
                    Utils.TestAction.MESSAGE_BODY_WRITER_THROW_WEB_APPLICATION,
                    Utils.TestAction.MESSAGE_BODY_WRITER_THROW_PROCESSING,
                    Utils.TestAction.MESSAGE_BODY_WRITER_THROW_ANY);
        }

        final OutputStreamWriter writer = new OutputStreamWriter(entityStream, MessageUtils.getCharset(mediaType));
        writer.write(Utils.FORMAT_PREFIX);
        writer.write(message.getText());
        writer.write(Utils.FORMAT_SUFFIX);
        writer.flush();
    }
}
