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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.message.MessageUtils;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Provider
@Consumes(Utils.APPLICATION_X_JERSEY_TEST)
public class MessageBodyReaderTestFormat implements MessageBodyReader<Message> {

    boolean serverSide = true;

    public MessageBodyReaderTestFormat() {
    }

    public MessageBodyReaderTestFormat(final boolean serverSide) {
        this.serverSide = serverSide;
    }

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType) {
        return type.isAssignableFrom(Message.class);
    }

    @Override
    public Message readFrom(final Class<Message> type, final Type genericType, final Annotation[] annotations,
                            final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                            final InputStream entityStream) throws IOException, WebApplicationException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream, MessageUtils.getCharset(mediaType)));

        final String line = reader.readLine();
        if (line == null || !line.startsWith(Utils.FORMAT_PREFIX) || !line.endsWith(Utils.FORMAT_SUFFIX)) {
            throw new WebApplicationException(
                    new IllegalArgumentException("Input content '" + line + "' is not in a valid format!"));
        }
        final String text = line.substring(Utils.FORMAT_PREFIX.length(), line.length() - Utils.FORMAT_SUFFIX.length());

        if (serverSide) {
            Utils.throwException(text, this,
                    Utils.TestAction.MESSAGE_BODY_READER_THROW_WEB_APPLICATION,
                    Utils.TestAction.MESSAGE_BODY_READER_THROW_PROCESSING,
                    Utils.TestAction.MESSAGE_BODY_READER_THROW_ANY);
        }

        return new Message(text);
    }
}
