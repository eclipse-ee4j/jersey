/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.message.internal.MessageBodyFactory;

/**
 * {@link javax.ws.rs.ext.MessageBodyReader} model.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.16
 */
public final class ReaderModel extends AbstractEntityProviderModel<MessageBodyReader> {

    /**
     * Create new reader model instance.
     *
     * NOTE: This constructor is package-private on purpose.
     *
     * @param provider modelled message body reader instance.
     * @param types    supported media types as declared in {@code @Consumes} annotation attached to the provider class.
     * @param custom   custom flag.
     */
    public ReaderModel(MessageBodyReader provider, List<MediaType> types, Boolean custom) {
        super(provider, types, custom, MessageBodyReader.class);
    }

    /**
     * Safely invokes {@link javax.ws.rs.ext.MessageBodyReader#isReadable isReadable} method on the underlying provider.
     *
     * Any exceptions will be logged at finer level.
     *
     * @param type        the class of instance to be produced.
     * @param genericType the type of instance to be produced. E.g. if the
     *                    message body is to be converted into a method parameter, this will be
     *                    the formal type of the method parameter as returned by
     *                    {@code Method.getGenericParameterTypes}.
     * @param annotations an array of the annotations on the declaration of the
     *                    artifact that will be initialized with the produced instance. E.g. if the
     *                    message body is to be converted into a method parameter, this will be
     *                    the annotations on that parameter returned by
     *                    {@code Method.getParameterAnnotations}.
     * @param mediaType   the media type of the HTTP entity, if one is not
     *                    specified in the request then {@code application/octet-stream} is
     *                    used.
     * @return {@code true} if the type is supported, otherwise {@code false}.
     */
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MessageBodyFactory.isReadable(super.provider(), type, genericType, annotations, mediaType);
    }
}
