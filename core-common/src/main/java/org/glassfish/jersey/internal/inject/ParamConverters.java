/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.internal.inject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.message.internal.HttpDateFormat;

/**
 * Container of several different {@link ParamConverterProvider param converter providers}
 * implementations. The nested provider implementations encapsulate various different
 * strategies of constructing an instance from a {@code String} value.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
@Singleton
public class ParamConverters {

    private static class ParamConverterCompliance {
        protected final boolean canReturnNull;

        private ParamConverterCompliance(boolean canReturnNull) {
            this.canReturnNull = canReturnNull;
        }

        protected <T> T nullOrThrow() {
            if (canReturnNull) {
                return null;
            } else {
                throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
            }
        }
    }

    private abstract static class AbstractStringReader<T> extends ParamConverterCompliance implements ParamConverter<T> {

        private AbstractStringReader(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public T fromString(final String value) {
            if (value == null) {
                return nullOrThrow();
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
                return nullOrThrow();
            }
            return value.toString();
        }

    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the target Java type instance
     * by invoking a single {@code String} parameter constructor on the target type.
     */
    @Singleton
    public static class StringConstructor extends ParamConverterCompliance implements ParamConverterProvider {

        private StringConstructor(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Constructor constructor = AccessController.doPrivileged(ReflectionHelper.getStringConstructorPA(rawType));

            return (constructor == null) ? null : new AbstractStringReader<T>(canReturnNull) {

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
    public static class TypeValueOf extends ParamConverterCompliance implements ParamConverterProvider {

        private TypeValueOf(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Method valueOf = AccessController.doPrivileged(ReflectionHelper.getValueOfStringMethodPA(rawType));

            return (valueOf == null) ? null : new AbstractStringReader<T>(canReturnNull) {

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
    public static class TypeFromString extends ParamConverterCompliance implements ParamConverterProvider {

        private TypeFromString(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {

            final Method fromStringMethod = AccessController.doPrivileged(ReflectionHelper.getFromStringStringMethodPA(rawType));

            return (fromStringMethod == null) ? null : new AbstractStringReader<T>(canReturnNull) {

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

        private TypeFromStringEnum(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            return (!Enum.class.isAssignableFrom(rawType)) ? null : super.getConverter(rawType, genericType, annotations);
        }
    }

    @Singleton
    public static class CharacterProvider extends ParamConverterCompliance implements ParamConverterProvider {

        private CharacterProvider(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            if (rawType.equals(Character.class)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(String value) {
                        if (value == null || value.isEmpty()) {
                            return CharacterProvider.this.nullOrThrow();
                        }

                        if (value.length() == 1) {
                            return rawType.cast(value.charAt(0));
                        }

                        throw new ExtractorException(LocalizationMessages.ERROR_PARAMETER_INVALID_CHAR_VALUE(value));
                    }

                    @Override
                    public String toString(T value) {
                        if (value == null) {
                            return CharacterProvider.this.nullOrThrow();
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
    public static class DateProvider extends ParamConverterCompliance implements ParamConverterProvider {

        private DateProvider(boolean canReturnNull) {
            super(canReturnNull);
        }

        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                  final Type genericType,
                                                  final Annotation[] annotations) {
            return (rawType != Date.class) ? null : new ParamConverter<T>() {

                @Override
                public T fromString(final String value) {
                    if (value == null) {
                        return DateProvider.this.nullOrThrow();
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
                        return DateProvider.this.nullOrThrow();
                    }
                    return value.toString();
                }
            };
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that convert the supplied string into a Java
     * {@link InputStream} instance.
     */
    public static class InputStreamProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            return rawType != InputStream.class ? null : new ParamConverter<T>() {

                @Override
                public T fromString(String value) {
                    if (value == null) {
                        throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                    }
                    return rawType.cast(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
                }

                @Override
                public String toString(T value) {
                    if (value == null) {
                        throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                    }
                    try {
                        return new String(((InputStream) value).readAllBytes());
                    } catch (IOException ioe) {
                        throw new ExtractorException(ioe);
                    }
                }
            };
        }
    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the Optional instance
     * by invoking {@link ParamConverterProvider}.
     */
    @Singleton
    public static class OptionalCustomProvider extends ParamConverterCompliance implements ParamConverterProvider {

        // Delegates to this provider when the type of Optional is extracted.
        private final InjectionManager manager;

        public OptionalCustomProvider(InjectionManager manager, boolean canReturnNull) {
            super(canReturnNull);
            this.manager = manager;
        }

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            return (rawType != Optional.class) ? null : new ParamConverter<T>() {

                @Override
                public T fromString(String value) {
                    if (value == null) {
                        return (T) Optional.empty();
                    } else {
                        final List<ClassTypePair> ctps = ReflectionHelper.getTypeArgumentAndClass(genericType);
                        final ClassTypePair ctp = (ctps.size() == 1) ? ctps.get(0) : null;
                        final boolean empty = value.isEmpty();
                        for (ParamConverterProvider provider : Providers.getProviders(manager, ParamConverterProvider.class)) {
                            final ParamConverter<?> converter = provider.getConverter(ctp.rawClass(), ctp.type(), annotations);
                            if (converter != null) {
                                if (empty) {
                                    return (T) Optional.empty();
                                } else {
                                    return (T) Optional.of(value).map(s -> converter.fromString(value));
                                }
                            }
                        }
                        /*
                         *  In this case we don't send Optional.empty() because 'value' is not null.
                         *  But we return null because the provider didn't find how to parse it.
                         */
                        return nullOrThrow();
                    }
                }

                @Override
                public String toString(T value) throws IllegalArgumentException {
                    /*
                     *  Unfortunately 'orElse' cannot be stored in an Optional. As only one value can
                     *  be stored, it makes no sense that 'value' is Optional. It can just be the value.
                     *  We don't fail here but we don't process it.
                     */
                    return null;
                }
            };
        }

    }

    /**
     * Provider of {@link ParamConverter param converter} that produce the OptionalInt, OptionalDouble
     * or OptionalLong instance.
     */
    @Singleton
    public static class OptionalProvider implements ParamConverterProvider {

        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            final Optionals optionals = Optionals.getOptional(rawType);
            return (optionals == null) ? null : new ParamConverter<T>() {

                @Override
                public T fromString(String value) {
                    if (value == null || value.isEmpty()) {
                        return (T) optionals.empty();
                    } else {
                        return (T) optionals.of(value);
                    }
                }

                @Override
                public String toString(T value) throws IllegalArgumentException {
                    /*
                     *  Unfortunately 'orElse' cannot be stored in an Optional. As only one value can
                     *  be stored, it makes no sense that 'value' is Optional. It can just be the value.
                     *  We don't fail here but we don't process it.
                     */
                    return null;
                }
            };
        }

        private static enum Optionals {

            OPTIONAL_INT(OptionalInt.class) {
                @Override
                Object empty() {
                    return OptionalInt.empty();
                }
                @Override
                Object of(Object value) {
                    return OptionalInt.of(Integer.parseInt((String) value));
                }
            }, OPTIONAL_DOUBLE(OptionalDouble.class) {
                @Override
                Object empty() {
                    return OptionalDouble.empty();
                }
                @Override
                Object of(Object value) {
                    return OptionalDouble.of(Double.parseDouble((String) value));
                }
            }, OPTIONAL_LONG(OptionalLong.class) {
                @Override
                Object empty() {
                    return OptionalLong.empty();
                }
                @Override
                Object of(Object value) {
                    return OptionalLong.of(Long.parseLong((String) value));
                }
            };

            private final Class<?> clazz;

            private Optionals(Class<?> clazz) {
                this.clazz = clazz;
            }

            private static Optionals getOptional(Class<?> clazz) {
                for (Optionals optionals : Optionals.values()) {
                    if (optionals.clazz == clazz) {
                        return optionals;
                    }
                }
                return null;
            }

            abstract Object empty();

            abstract Object of(Object value);
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
        @Inject
        public AggregatedProvider(@Context InjectionManager manager, @Context Configuration configuration) {
            boolean canThrowNull = !CommonProperties.getValue(configuration.getProperties(),
                    CommonProperties.PARAM_CONVERTERS_THROW_IAE,
                    Boolean.FALSE);
            this.providers = new ParamConverterProvider[] {
                    // ordering is important (e.g. Date provider must be executed before String Constructor
                    // as Date has a deprecated String constructor
                    new DateProvider(canThrowNull),
                    new TypeFromStringEnum(canThrowNull),
                    new TypeValueOf(canThrowNull),
                    new CharacterProvider(canThrowNull),
                    new InputStreamProvider(),
                    new TypeFromString(canThrowNull),
                    new StringConstructor(canThrowNull),
                    new OptionalCustomProvider(manager, canThrowNull),
                    new OptionalProvider()
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
