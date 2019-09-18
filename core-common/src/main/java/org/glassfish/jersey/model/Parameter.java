/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.model;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.guava.Lists;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.model.internal.spi.ParameterServiceProvider;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parameter implements AnnotatedElement {
    private static final Logger LOGGER = Logger.getLogger(Parameter.class.getName());

    /**
     * Parameter injection sources type.
     */
    public enum Source {

        /**
         * Context parameter injection source.
         */
        CONTEXT,
        /**
         * Cookie parameter injection source.
         */
        COOKIE,
        /**
         * Entity parameter injection source.
         */
        ENTITY,
        /**
         * Form parameter injection source.
         */
        FORM,
        /**
         * Header parameter injection source.
         */
        HEADER,
        /**
         * Uri parameter injection source.
         */
        URI,
        /**
         * Matrix parameter injection source.
         */
        MATRIX,
        /**
         * Path parameter injection source.
         */
        PATH,
        /**
         * Query parameter injection source.
         */
        QUERY,
        /**
         * Suspended async response injection source.
         */
        SUSPENDED,
        /**
         * Bean param parameter injection source.
         */
        BEAN_PARAM,
        /**
         * Unknown parameter injection source.
         */
        UNKNOWN
    }

    static {
        List<ParameterServiceProvider> PARAMETER_SERVICE_PROVIDERS = Lists
                .newArrayList(ServiceFinder.find(ParameterServiceProvider.class));
        PARAMETER_SERVICE_PROVIDERS.add(new ParameterService());
        PARAM_CREATION_FACTORIES = Collections.unmodifiableList(
                PARAMETER_SERVICE_PROVIDERS.stream().map(a -> a.getParameterCreationFactory())
                        .collect(Collectors.toList())
        );
        ANNOTATION_HELPER_MAP = Collections.unmodifiableMap(
                PARAMETER_SERVICE_PROVIDERS.stream()
                        .map(a -> a.getParameterAnnotationHelperMap())
                        .collect(WeakHashMap::new, Map::putAll, Map::putAll)
        );
    };
    private static final List<ParamCreationFactory> PARAM_CREATION_FACTORIES;
    private static final Map<Class, ParamAnnotationHelper> ANNOTATION_HELPER_MAP;

    public interface ParamAnnotationHelper<T extends Annotation> {

        public String getValueOf(T a);

        public Parameter.Source getSource();
    }

    /**
     * A factory service to found in a runtime to be used to instantiate the given {@link Parameter} class.
     * @param <PARAMETER> the {@code Parameter} to be instatiated
     */
    public interface ParamCreationFactory<PARAMETER extends Parameter> {
        /**
         * Determine whether the Factory is for the given class to be instantiated.
         * @param clazz The class of determining the source of origin (core, server). Each source of origin
         *              has its own {@code ParamCreationFactory}
         * @return {@code true} if the source of origin corresponds to the {@code ParamCreationFactory},
         *          {@code false} otherwise.
         */
        boolean isFor(Class<?> clazz);

        /**
         * Factory method to instantiate {@link Parameter} of given properties
         * @return instantiated {@code Parameter}
         */
        PARAMETER createParameter(Annotation[] markers, Annotation marker, Source source, String sourceName, Class<?> rawType,
                                  Type type, boolean encoded, String defaultValue);

        /**
         * Factory method to instantiate {@code BeanParameter} of given properties
         * @return instantiated {@code BeanParameter}
         */
        PARAMETER createBeanParameter(Annotation[] markers, Annotation marker, Source source, String sourceName, Class<?> rawType,
                                      Type type, boolean encoded, String defaultValue);
    }

    // Instance
    private final Annotation[] annotations;
    private final Annotation sourceAnnotation;
    private final Parameter.Source source;
    private final String sourceName;
    private final boolean encoded;
    private final String defaultValue;
    private final Class<?> rawType;
    private final Type type;

    protected Parameter(
            Annotation[] markers,
            Annotation marker,
            Source source,
            String sourceName,
            Class<?> rawType,
            Type type,
            boolean encoded,
            String defaultValue) {
        this.annotations = markers;
        this.sourceAnnotation = marker;
        this.source = source;
        this.sourceName = sourceName;
        this.rawType = rawType;
        this.type = type;
        this.encoded = encoded;
        this.defaultValue = defaultValue;
    }
    /**
     * Create a parameter model.
     *
     * @param concreteClass   concrete resource method handler implementation class.
     * @param declaringClass  declaring class of the method the parameter belongs to or field that this parameter represents.
     * @param encodeByDefault flag indicating whether the parameter should be encoded by default or not. Note that a presence
     *                        of {@link Encoded} annotation in the list of the parameter {@code annotations} will override any
     *                        value set in the flag to {@code true}.
     * @param rawType         raw Java parameter type.
     * @param type            generic Java parameter type.
     * @param annotations     parameter annotations.
     * @return new parameter model.
     */
    @SuppressWarnings("unchecked")
    public static <PARAMETER extends Parameter> PARAMETER create(
            Class concreteClass,
            Class declaringClass,
            boolean encodeByDefault,
            Class<?> rawType,
            Type type,
            Annotation[] annotations) {
        return (PARAMETER) create(concreteClass, declaringClass, encodeByDefault, rawType, type, annotations, Parameter.class);
    }

    /**
     * Create a parameter model.
     *
     * @param concreteClass   concrete resource method handler implementation class.
     * @param declaringClass  declaring class of the method the parameter belongs to or field that this parameter represents.
     * @param encodeByDefault flag indicating whether the parameter should be encoded by default or not. Note that a presence
     *                        of {@link Encoded} annotation in the list of the parameter {@code annotations} will override any
     *                        value set in the flag to {@code true}.
     * @param rawType         raw Java parameter type.
     * @param type            generic Java parameter type.
     * @param annotations     parameter annotations.
     * @param parameterClass  class of the parameter to be used by {@link ParamCreationFactory}
     * @return new parameter model.
     */
    @SuppressWarnings("unchecked")
    protected static <PARAMETER extends Parameter> PARAMETER create(
            Class concreteClass,
            Class declaringClass,
            boolean encodeByDefault,
            Class<?> rawType,
            Type type,
            Annotation[] annotations,
            Class<?> parameterClass) {

        if (null == annotations) {
            return null;
        }

        Annotation paramAnnotation = null;
        Parameter.Source paramSource = null;
        String paramName = null;
        boolean paramEncoded = encodeByDefault;
        String paramDefault = null;

        /**
         * Create a parameter from the list of annotations. Unknown annotated
         * parameters are also supported, and in such a cases the last
         * unrecognized annotation is taken to be that associated with the
         * parameter.
         */
        for (Annotation annotation : annotations) {
            if (ANNOTATION_HELPER_MAP.containsKey(annotation.annotationType())) {
                ParamAnnotationHelper helper = ANNOTATION_HELPER_MAP.get(annotation.annotationType());
                paramAnnotation = annotation;
                paramSource = helper.getSource();
                paramName = helper.getValueOf(annotation);
            } else if (Encoded.class == annotation.annotationType()) {
                paramEncoded = true;
            } else if (DefaultValue.class == annotation.annotationType()) {
                paramDefault = ((DefaultValue) annotation).value();
            } else {
                // Take latest unknown annotation, but don't override known annotation
                if ((paramAnnotation == null) || (paramSource == Source.UNKNOWN)) {
                    paramAnnotation = annotation;
                    paramSource = Source.UNKNOWN;
                    paramName = getValue(annotation);
                }
            }
        }

        if (paramAnnotation == null) {
            paramSource = Parameter.Source.ENTITY;
        }

        ClassTypePair ct = ReflectionHelper.resolveGenericType(
                concreteClass, declaringClass, rawType, type);

        if (paramSource == Source.BEAN_PARAM) {
            return createBeanParameter(
                    annotations,
                    paramAnnotation,
                    paramSource,
                    paramName,
                    ct.rawClass(),
                    ct.type(),
                    paramEncoded,
                    paramDefault,
                    parameterClass);
        } else {
            return createParameter(
                    annotations,
                    paramAnnotation,
                    paramSource,
                    paramName,
                    ct.rawClass(),
                    ct.type(),
                    paramEncoded,
                    paramDefault,
                    parameterClass);
        }
    }

    private static <PARAMETER extends Parameter>  PARAMETER createBeanParameter(Annotation[] markers, Annotation marker,
                                                                                 Source source, String sourceName,
                                                                                 Class<?> rawType, Type type, boolean encoded,
                                                                                 String defaultValue,
                                                                                 Class<?> parameterClass) {
        for (ParamCreationFactory<?> factory : PARAM_CREATION_FACTORIES) {
            if (factory.isFor(parameterClass)) {
                return (PARAMETER) factory.createBeanParameter(markers, marker, source, sourceName, rawType, type, encoded,
                        defaultValue);
            }
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, LocalizationMessages.PARAM_CREATION_FACTORY_NOT_FOUND(parameterClass.getName()));
        }
        throw new IllegalStateException(LocalizationMessages.PARAM_CREATION_FACTORY_NOT_FOUND(parameterClass.getName()));
    }

    private static <PARAMETER extends Parameter> PARAMETER createParameter(Annotation[] markers, Annotation marker,
                                                                             Source source, String sourceName, Class<?> rawType,
                                                                             Type type, boolean encoded, String defaultValue,
                                                                             Class<?> parameterClass) {
        for (ParamCreationFactory<?> factory : PARAM_CREATION_FACTORIES) {
            if (factory.isFor(parameterClass)) {
                return (PARAMETER) factory.createParameter(markers, marker, source, sourceName, rawType, type, encoded,
                        defaultValue);
            }
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, LocalizationMessages.PARAM_CREATION_FACTORY_NOT_FOUND(parameterClass.getName()));
        }
        throw new IllegalStateException(LocalizationMessages.PARAM_CREATION_FACTORY_NOT_FOUND(parameterClass.getName()));
    }

    /**
     * Create a list of parameter models for a given Java method handling a resource
     * method, sub-resource method or a sub-resource locator.
     *
     * @param concreteClass  concrete resource method handler implementation class.
     * @param declaringClass the class declaring the handling Java method.
     * @param javaMethod     Java method handling a resource method, sub-resource
     *                       method or a sub-resource locator.
     * @param keepEncoded    set to {@code true} to disable automatic decoding
     *                       of all the method parameters. (See {@link Encoded}.
     * @return a list of handling method parameter models.
     */
    public static <PARAMETER extends Parameter> List<PARAMETER> create(
            Class concreteClass,
            Class declaringClass,
            Method javaMethod,
            boolean keepEncoded) {
        return createList(concreteClass, declaringClass, javaMethod, keepEncoded, Parameter.class);
    }

    /**
     * Create a list of parameter models for a given Java method handling a resource
     * method, sub-resource method or a sub-resource locator.
     *
     * @param concreteClass  concrete resource method handler implementation class.
     * @param declaringClass the class declaring the handling Java method.
     * @param javaMethod     Java method handling a resource method, sub-resource
     *                       method or a sub-resource locator.
     * @param keepEncoded    set to {@code true} to disable automatic decoding
     *                       of all the method parameters. (See {@link Encoded}.
     * @param parameterClass Class of a Parameter in returned list
     * @return a list of handling method parameter models.
     */
    protected static <PARAMETER extends Parameter> List<PARAMETER> createList(
            Class concreteClass,
            Class declaringClass,
            Method javaMethod,
            boolean keepEncoded,
            Class parameterClass) {
        AnnotatedMethod method = new AnnotatedMethod(javaMethod);

        return createList(
                concreteClass, declaringClass,
                ((null != method.getAnnotation(Encoded.class)) || keepEncoded),
                method.getParameterTypes(),
                method.getGenericParameterTypes(),
                method.getParameterAnnotations(),
                parameterClass);
    }

    private static <PARAMETER extends Parameter> List<PARAMETER> createList(Class concreteClass, Class declaringClass,
                                                                            boolean keepEncoded, Class[] parameterTypes,
                                                                            Type[] genericParameterTypes,
                                                                            Annotation[][] parameterAnnotations,
                                                                            Class<?> parameterClass) {
        final List<PARAMETER> parameters = new ArrayList<>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            final PARAMETER parameter = Parameter.create(concreteClass, declaringClass, keepEncoded, parameterTypes[i],
                    genericParameterTypes[i], parameterAnnotations[i], parameterClass);
            if (null != parameter) {
                parameters.add(parameter);
            } else {
                // TODO throw IllegalStateException instead?
                return Collections.emptyList();
            }
        }

        return parameters;
    }

    /**
     * Create a list of parameter models for a given resource method handler
     * injectable constructor.
     *
     * @param concreteClass  concrete resource method handler implementation class.
     * @param declaringClass class where the method has been declared.
     * @param ctor           injectable constructor of the resource method handler.
     * @param keepEncoded    set to {@code true} to disable automatic decoding
     *                       of all the constructor parameters. (See {@link Encoded}.
     * @return a list of constructor parameter models.
     */
    protected static <PARAMETER extends org.glassfish.jersey.model.Parameter> List<PARAMETER> createList(
            Class concreteClass,
            Class declaringClass,
            Constructor<?> ctor,
            boolean keepEncoded,
            Class<?> parameterClass) {

        Class[] parameterTypes = ctor.getParameterTypes();
        Type[] genericParameterTypes = ctor.getGenericParameterTypes();
        // Workaround bug http://bugs.sun.com/view_bug.do?bug_id=5087240
        if (parameterTypes.length != genericParameterTypes.length) {
            Type[] _genericParameterTypes = new Type[parameterTypes.length];
            _genericParameterTypes[0] = parameterTypes[0];
            System.arraycopy(genericParameterTypes, 0, _genericParameterTypes, 1, genericParameterTypes.length);
            genericParameterTypes = _genericParameterTypes;
        }

        return createList(
                concreteClass, declaringClass,
                ((null != ctor.getAnnotation(Encoded.class)) || keepEncoded),
                parameterTypes,
                genericParameterTypes,
                ctor.getParameterAnnotations(),
                parameterClass);
    }


    private static String getValue(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("value");
            if (m.getReturnType() != String.class) {
                return null;
            }
            return (String) m.invoke(a);
        } catch (Exception ex) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER,
                        String.format("Unable to get the %s annotation value property", a.getClass().getName()), ex);
            }
        }
        return null;
    }

    /**
     * Get the parameter source annotation.
     *
     * @return parameter source annotation.
     */
    public Annotation getSourceAnnotation() {
        return sourceAnnotation;
    }

    /**
     * Get the parameter value source type.
     *
     * @return parameter value source type.
     */
    public Parameter.Source getSource() {
        return source;
    }

    /**
     * Get the parameter source name, i.e. value of the parameter source annotation.
     *
     * @return parameter source name.
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * If {@code true}, the injected parameter value should remain encoded.
     *
     * @return {@code true} if the parameter value should remain encoded,
     * {@code false} otherwise.
     */
    public boolean isEncoded() {
        return encoded;
    }

    /**
     * Check if the parameter has a default value set.
     *
     * @return {@code true} if the default parameter value has been set,
     * {@code false} otherwise.
     */
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    /**
     * Get the default parameter value.
     *
     * @return default parameter value or {@code null} if no default value has
     * been set for the parameter.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get raw type information for the parameter.
     *
     * @return raw parameter type information.
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * Get generic type information for the parameter.
     *
     * @return generic parameter type information.
     */
    public Type getType() {
        return type;
    }

    /**
     * Check if the parameter is qualified.
     *
     * @return {@code false}.
     */
    public boolean isQualified() {
        return false;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null) {
            return null;
        }
        for (Annotation a : annotations) {
            if (a.annotationType() == annotationClass) {
                return annotationClass.cast(a);
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.clone();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotations.clone();
    }

    @Override
    public String toString() {
        return String.format("Parameter [type=%s, source=%s, defaultValue=%s]",
                getRawType(), getSourceName(), getDefaultValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Parameter parameter = (Parameter) o;

        if (encoded != parameter.encoded) {
            return false;
        }
        if (!Arrays.equals(annotations, parameter.annotations)) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(parameter.defaultValue) : parameter.defaultValue != null) {
            return false;
        }
        if (rawType != null ? !rawType.equals(parameter.rawType) : parameter.rawType != null) {
            return false;
        }
        if (source != parameter.source) {
            return false;
        }
        if (sourceAnnotation != null ? !sourceAnnotation.equals(parameter.sourceAnnotation) : parameter.sourceAnnotation
                != null) {
            return false;
        }
        if (sourceName != null ? !sourceName.equals(parameter.sourceName) : parameter.sourceName != null) {
            return false;
        }
        if (type != null ? !type.equals(parameter.type) : parameter.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = annotations != null ? Arrays.hashCode(annotations) : 0;
        result = 31 * result + (sourceAnnotation != null ? sourceAnnotation.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
        result = 31 * result + (encoded ? 1 : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (rawType != null ? rawType.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public static class ParameterService implements ParameterServiceProvider {
        @Override
        public Map<Class, ParamAnnotationHelper> getParameterAnnotationHelperMap() {
            Map<Class, ParamAnnotationHelper> m = new WeakHashMap<Class, ParamAnnotationHelper>();
            m.put(Context.class, new ParamAnnotationHelper<Context>() {

                @Override
                public String getValueOf(Context a) {
                    return null;
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.CONTEXT;
                }
            });
            m.put(CookieParam.class, new ParamAnnotationHelper<CookieParam>() {

                @Override
                public String getValueOf(CookieParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.COOKIE;
                }
            });
            m.put(FormParam.class, new ParamAnnotationHelper<FormParam>() {

                @Override
                public String getValueOf(FormParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.FORM;
                }
            });
            m.put(HeaderParam.class, new ParamAnnotationHelper<HeaderParam>() {

                @Override
                public String getValueOf(HeaderParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.HEADER;
                }
            });
            m.put(MatrixParam.class, new ParamAnnotationHelper<MatrixParam>() {

                @Override
                public String getValueOf(MatrixParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.MATRIX;
                }
            });
            m.put(PathParam.class, new ParamAnnotationHelper<PathParam>() {

                @Override
                public String getValueOf(PathParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.PATH;
                }
            });
            m.put(QueryParam.class, new ParamAnnotationHelper<QueryParam>() {

                @Override
                public String getValueOf(QueryParam a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.QUERY;
                }
            });
            m.put(Suspended.class, new ParamAnnotationHelper<Suspended>() {

                @Override
                public String getValueOf(Suspended a) {
                    return Suspended.class.getName();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.SUSPENDED;
                }
            });
            m.put(BeanParam.class, new ParamAnnotationHelper<BeanParam>() {

                @Override
                public String getValueOf(BeanParam a) {
                    return null;
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.BEAN_PARAM;
                }
            });
            return m;
        }

        @Override
        public ParamCreationFactory<Parameter> getParameterCreationFactory() {
            return new ParamCreationFactory<Parameter>() {
                @Override
                public boolean isFor(Class<?> clazz) {
                    return clazz == Parameter.class;
                }

                @Override
                public Parameter createParameter(Annotation[] markers, Annotation marker, Source source, String sourceName,
                                                 Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                    return new Parameter(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
                }

                @Override
                public Parameter createBeanParameter(Annotation[] markers, Annotation marker, Source source, String sourceName,
                                                     Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                    return createParameter(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
                }
            };
        }
    }
}
