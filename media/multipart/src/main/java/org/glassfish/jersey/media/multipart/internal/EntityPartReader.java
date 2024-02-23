/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.JerseyEntityPartBuilderProvider;
import org.glassfish.jersey.media.multipart.MultiPart;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Reader supporting List&lt;EntityPart&gt; Make sure {@code GenericEntity} class is used when reading the
 * {@link EntityPart}s.
 * @since 3.1.0
 */
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Singleton
public class EntityPartReader implements MessageBodyReader<List<EntityPart>> {

    private MultiPartReaderClientSide multiPartReaderClientSide;

    private final Providers providers;

    @Inject
    public EntityPartReader(@Context Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isReadable(Class<?> type, Type generic, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(type)
                && ParameterizedType.class.isInstance(generic)
                && EntityPart.class.isAssignableFrom(ReflectionHelper.getGenericTypeArgumentClasses(generic).get(0));
    }

    @Override
    public List<EntityPart> readFrom(Class<List<EntityPart>> type, Type genericType, Annotation[] annotations,
                                     MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream) throws IOException, WebApplicationException {

        if (multiPartReaderClientSide == null) {
            multiPartReaderClientSide = (MultiPartReaderClientSide) providers.getMessageBodyReader(
                    MultiPart.class, MultiPart.class, new Annotation[0], MediaType.MULTIPART_FORM_DATA_TYPE);
        }

        final MultiPart multiPart = multiPartReaderClientSide.readFrom(
                MultiPart.class, MultiPart.class, annotations, mediaType, httpHeaders, entityStream);
        final List<BodyPart> bodyParts = multiPart.getBodyParts();
        final List<EntityPart> entityParts = new LinkedList<>();

        for (BodyPart bp : bodyParts) {
            if (FormDataBodyPart.class.isInstance(bp)) {
                entityParts.add((EntityPart) bp);
            } else {
                final EntityPart ep = new JerseyEntityPartBuilderProvider().withName("")
                        .mediaType(bp.getMediaType()).content(bp.getEntity()).headers(bp.getHeaders()).build();
                entityParts.add(ep);
            }
        }

        return entityParts;
    }
}
