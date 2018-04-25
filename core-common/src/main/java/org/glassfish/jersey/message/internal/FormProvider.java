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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.util.collection.NullableMultivaluedHashMap;

/**
 * Provider for marshalling/un-marshalling of {@code application/x-www-form-urlencoded}
 * entity type to/from {@link Form JAX-RS Form} instance.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Produces({"application/x-www-form-urlencoded", "*/*"})
@Consumes({"application/x-www-form-urlencoded", "*/*"})
@Singleton
public final class FormProvider extends AbstractFormProvider<Form> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Form.class;
    }

    @Override
    public Form readFrom(
            Class<Form> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException {

        return new Form(readFrom(new NullableMultivaluedHashMap<String, String>(), mediaType, decode(annotations), entityStream));
    }


    private boolean decode(Annotation annotations[]) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Encoded.class)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Form.class;
    }

    @Override
    public void writeTo(
            Form t,
            Class<?> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        writeTo(t.asMap(), mediaType, entityStream);
    }
}
