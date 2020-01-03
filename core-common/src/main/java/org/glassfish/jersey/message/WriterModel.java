/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.internal.MessageBodyFactory;

/**
 * {@link javax.ws.rs.ext.MessageBodyWriter} model.
 *
 * @author Marek Potociar
 * @since 2.16
 */
public final class WriterModel extends AbstractEntityProviderModel<MessageBodyWriter> {
    /**
     * Create new writer model instance.
     *
     * NOTE: This constructor is package-private on purpose.
     *
     * @param provider modelled message body writer instance.
     * @param types    supported media types as declared in {@code @Consumes} annotation attached to the provider class.
     * @param custom   custom flag.
     */
    public WriterModel(MessageBodyWriter provider, List<MediaType> types, Boolean custom) {
        super(provider, types, custom, MessageBodyWriter.class);
    }

    /**
     * Safely invokes {@link javax.ws.rs.ext.MessageBodyWriter#isWriteable isWriteable} method on the underlying provider.
     *
     * Any exceptions will be logged at finer level.
     *
     * @param type        the class of instance that is to be written.
     * @param genericType the type of instance to be written, obtained either
     *                    by reflection of a resource method return type or via inspection
     *                    of the returned instance. {@link javax.ws.rs.core.GenericEntity}
     *                    provides a way to specify this information at runtime.
     * @param annotations an array of the annotations attached to the message entity instance.
     * @param mediaType   the media type of the HTTP entity.
     * @return {@code true} if the type is supported, otherwise {@code false}.
     */
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MessageBodyFactory.isWriteable(super.provider(), type, genericType, annotations, mediaType);
    }
}
