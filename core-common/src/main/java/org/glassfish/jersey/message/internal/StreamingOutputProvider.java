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

package org.glassfish.jersey.message.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.inject.Singleton;

/**
 * Message body writer that supports {@link StreamingOutput streaming output} marshalling.
 *
 * @author Paul Sandoz
 */
@Produces({"application/octet-stream", "*/*"})
@Singleton
public final class StreamingOutputProvider implements MessageBodyWriter<StreamingOutput> {

    @Override
    public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
        return StreamingOutput.class.isAssignableFrom(t);
    }

    @Override
    public long getSize(StreamingOutput o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(StreamingOutput o, Class<?> t, Type gt, Annotation[] as,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream entity) throws IOException {
        o.write(entity);
    }
}
