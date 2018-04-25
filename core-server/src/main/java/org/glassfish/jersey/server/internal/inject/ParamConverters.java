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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.text.ParseException;
import java.util.Date;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.internal.HttpDateFormat;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Container of several different {@link ParamConverterProvider param converter providers}
 * implementations. The nested provider implementations encapsulate various different
 * strategies of constructing an instance from a {@code String} value.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
class ParamConverters {

    private abstract static class AbstractStringReader<T> implements ParamConverter<T> {

        @Override
        public T fromString(final String value) {
            if (value == null) {
                throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
            }
            try {
                return _fromString(value);
            } catch (final InvocationTargetException ex) {
                // if the value is an empty string, return null
                if (value.isEmpty()) {
                    return null;
                }
                final Throwable cause = ex.getCause();
                if (cause instanceof WebApplicationException) {
                    throw (WebApplicationException) cause;
                } else {
                    throw new ExtractorException(cause);
                }
            } catch (final Exception ex) {
                throw new ProcessingException(ex);
            }
        }

        protected abstract T _fromString(String value) throws Exception;

        @Override
        public String toString(final T value) throws IllegalArgumentException {
            if (value == null) {
                throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
            }
            return value.toString();
        }

    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java type instance
     * by invoking a single {@code String} parameter constructor on the target type.
     */
    @Singleton
    public static class StringConstructor implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Constructor constructor = AccessController.doPrivileged(ReflectionHelper.getStringConstructorPA(rawType));

            return (constructor == null) ? null : new AbstractStringReader<T>() {

                @Override
                protected T _fromString(final String value) throws Exception {
                    return rawType.cast(constructor.newInstance(value));
                }
            };
        }

    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java type instance
     * by invoking a static {@code valueOf(String)} method on the target type.
     */
    @Singleton
    public static class TypeValueOf implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Method valueOf = AccessController.doPrivileged(ReflectionHelper.getValueOfStringMethodPA(rawType));

            return (valueOf == null) ? null : new AbstractStringReader<T>() {

                @Override
                public T _fromString(final String value) throws Exception {
                    return rawType.cast(valueOf.invoke(null, value));
                }
            };
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java type instance
     * by invoking a static {@code fromString(String)} method on the target type.
     */
    @Singleton
    public static class TypeFromString implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Method fromStringMethod = AccessController.doPrivileged(ReflectionHelper.getFromStringStringMethodPA(rawType));

            return (fromStringMethod == null) ? null : new AbstractStringReader<T>() {

                @Override
                public T _fromString(final String value) throws Exception {
                    return rawType.cast(fromStringMethod.invoke(null, value));
                }
            };
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java {@link Enum enum} type instance
     * by invoking a static {@code fromString(String)} method on the target enum type.
     */
    @Singleton
    public static class TypeFromStringEnum extends TypeFromString {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            return (!Enum.class.isAssignableFrom(rawType)) ? null : super.getConverter(rawType, genericType, annotations);
        }
    }

    @Singleton
    public static class CharacterProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType.equals(Character.class)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(String value) {
                        if (value == null || value.isEmpty()) {
                            return null;
                            // throw new IllegalStateException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                        }

                        if (value.length() == 1) {
                            return rawType.cast(value.charAt(0));
                        }

                        throw new ExtractorException(LocalizationMessages.ERROR_PARAMETER_INVALID_CHAR_VALUE(value));
                    }

                    @Override
                    public String toString(T value) {
                        if (value == null) {
                            throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                        }
                        return value.toString();
                    }
                };
            }

            return null;
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that convert the supplied string into a Java
     * {@link Date} instance using conversion method from the
     * {@link HttpDateFormat http date formatter} utility class.
     */
    @Singleton
    public static class DateProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            return (rawType != Date.class) ? null : new ParamConverter<T>() {

                @Override
                public T fromString(final String value) {
                    if (value == null) {
                        throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                    }
                    try {
                        return rawType.cast(HttpDateFormat.readDate(value));
                    } catch (final ParseException ex) {
                        throw new ExtractorException(ex);
                    }
                }

                @Override
                public String toString(final T value) throws IllegalArgumentException {
                    if (value == null) {
                        throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                    }
                    return value.toString();
                }
            };
        }
    }

    /**
     * Aggregated {@link ParamConverterProvider param converter provider}.
     */
    @Singleton
    public static class AggregatedProvider implements ParamConverterProvider {

        private final ParamConverterProvider[] providers;

        /**
         * Create new aggregated {@link ParamConverterProvider param converter provider}.
         */
        public AggregatedProvider() {
            providers = new ParamConverterProvider[] {
                    // ordering is important (e.g. Date provider must be executed before String Constructor
                    // as Date has a deprecated String constructor
                    new DateProvider(),
                    new TypeFromStringEnum(),
                    new TypeValueOf(),
                    new CharacterProvider(),
                    new TypeFromString(),
                    new StringConstructor()
            };
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            for (final ParamConverterProvider p : providers) {
                // This iteration trough providers is important. It can't be replaced by just registering all the internal
                // providers of this class. Using iteration trough array the correct ordering of providers is ensured (see
                // javadoc of PathParam, HeaderParam, ... - there is defined a fixed order of constructing objects form Strings).
                final ParamConverter<T> reader = p.getConverter(rawType, genericType, annotations);
                if (reader != null) {
                    return reader;
                }
            }
            return null;
        }
    }
}
