/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart.internal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Writer supporting List&lt;EntityPart&gt; Make sure {@code GenericEntity} class is used when sending the
 * {@link EntityPart}s.
 * @since 3.1.0
 */
@Produces(MediaType.MULTIPART_FORM_DATA)
@Singleton
public class EntityPartWriter implements MessageBodyWriter<List<EntityPart>> {

    private MultiPartWriter multiPartWriter;

    @Inject
    Providers providers;

    @Override
    public boolean isWriteable(Class<?> type, Type generic, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type)
                && ParameterizedType.class.isInstance(generic)
                && EntityPart.class.isAssignableFrom(ReflectionHelper.getGenericTypeArgumentClasses(generic).get(0));
    }

    @Override
    public void writeTo(List<EntityPart> entityParts, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        final MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(mediaType);
        for (EntityPart ep : entityParts) {
            multiPart.bodyPart((BodyPart) ep);
        }

        if (multiPartWriter == null) {
            multiPartWriter = (MultiPartWriter) providers.getMessageBodyWriter(
                    MultiPart.class, MultiPart.class, new Annotation[0], MediaType.MULTIPART_FORM_DATA_TYPE);
        }

        multiPartWriter.writeTo(multiPart, MultiPart.class, MultiPart.class,
                annotations, multiPart.getMediaType(), httpHeaders, entityStream);
    }
}
