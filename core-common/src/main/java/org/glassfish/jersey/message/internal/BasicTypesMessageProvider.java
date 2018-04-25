/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * The basic types message body provider for {@link MediaType#TEXT_PLAIN} media type.
 * <p/>
 * The provider processes primitive types and also other {@link Number} implementations like {@link java.math.BigDecimal},
 * {@link java.math.BigInteger}, {@link AtomicInteger},  {@link AtomicLong} and all other implementations which has one String
 * argument constructor.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Produces({"text/plain"})
@Consumes({"text/plain"})
@Singleton
final class BasicTypesMessageProvider extends AbstractMessageReaderWriterProvider<Object> {

    private static enum PrimitiveTypes {
        BYTE(Byte.class, byte.class) {
            @Override
            public Object convert(String s) {
                return Byte.valueOf(s);
            }
        },
        SHORT(Short.class, short.class) {
            @Override
            public Object convert(String s) {
                return Short.valueOf(s);
            }
        },
        INTEGER(Integer.class, int.class) {
            @Override
            public Object convert(String s) {
                return Integer.valueOf(s);
            }
        },
        LONG(Long.class, long.class) {
            @Override
            public Object convert(String s) {
                return Long.valueOf(s);
            }
        },
        FLOAT(Float.class, float.class) {
            @Override
            public Object convert(String s) {
                return Float.valueOf(s);
            }
        },
        DOUBLE(Double.class, double.class) {
            @Override
            public Object convert(String s) {
                return Double.valueOf(s);
            }
        },
        BOOLEAN(Boolean.class, boolean.class) {
            @Override
            public Object convert(String s) {
                return Boolean.valueOf(s);
            }
        },
        CHAR(Character.class, char.class) {
            @Override
            public Object convert(String s) {
                if (s.length() != 1) {
                    throw new MessageBodyProcessingException(LocalizationMessages
                            .ERROR_ENTITY_PROVIDER_BASICTYPES_CHARACTER_MORECHARS());
                }
                return s.charAt(0);
            }
        };

        public static PrimitiveTypes forType(Class<?> type) {
            for (PrimitiveTypes primitive : PrimitiveTypes.values()) {
                if (primitive.supports(type)) {
                    return primitive;
                }
            }
            return null;
        }

        private final Class<?> wrapper;
        private final Class<?> primitive;

        private PrimitiveTypes(Class<?> wrapper, Class<?> primitive) {
            this.wrapper = wrapper;
            this.primitive = primitive;
        }

        public abstract Object convert(String s);

        public boolean supports(Class<?> type) {
            return type == wrapper || type == primitive;
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return canProcess(type);
    }

    @Override
    public Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) throws IOException, WebApplicationException {
        final String entityString = readFromAsString(entityStream, mediaType);
        if (entityString.isEmpty()) {
            throw new NoContentException(LocalizationMessages.ERROR_READING_ENTITY_MISSING());
        }
        final PrimitiveTypes primitiveType = PrimitiveTypes.forType(type);
        if (primitiveType != null) {
            return primitiveType.convert(entityString);
        }

        final Constructor constructor = AccessController.doPrivileged(ReflectionHelper.getStringConstructorPA(type));
        if (constructor != null) {
            try {
                return type.cast(constructor.newInstance(entityString));
            } catch (Exception e) {
                throw new MessageBodyProcessingException(LocalizationMessages.ERROR_ENTITY_PROVIDER_BASICTYPES_CONSTRUCTOR(type));
            }
        }

        if (AtomicInteger.class.isAssignableFrom(type)) {
            return new AtomicInteger((Integer) PrimitiveTypes.INTEGER.convert(entityString));
        }

        if (AtomicLong.class.isAssignableFrom(type)) {
            return new AtomicLong((Long) PrimitiveTypes.LONG.convert(entityString));
        }

        throw new MessageBodyProcessingException(LocalizationMessages.ERROR_ENTITY_PROVIDER_BASICTYPES_UNKWNOWN(type));
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return canProcess(type);

    }

    private boolean canProcess(Class<?> type) {
        if (PrimitiveTypes.forType(type) != null) {
            return true;
        }
        if (Number.class.isAssignableFrom(type)) {
            final Constructor constructor = AccessController.doPrivileged(ReflectionHelper.getStringConstructorPA(type));
            if (constructor != null) {
                return true;
            }
            if (AtomicInteger.class.isAssignableFrom(type) || AtomicLong.class.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return t.toString().length();
    }

    @Override
    public void writeTo(
            Object o,
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        writeToAsString(o.toString(), entityStream, mediaType);
    }
}
