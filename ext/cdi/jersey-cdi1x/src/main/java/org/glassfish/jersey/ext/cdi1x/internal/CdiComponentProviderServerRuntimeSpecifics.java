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

package org.glassfish.jersey.ext.cdi1x.internal;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Qualifier;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Server side runtime CDI ComponentProvider specific implementation.
 */
class CdiComponentProviderServerRuntimeSpecifics implements CdiComponentProviderRuntimeSpecifics {
    /*
     * annotation types that distinguish the classes to be added to {@link CdiComponentProvider#jaxrsInjectableTypes}
     */
    private static final Set<Class<? extends Annotation>> JAX_RS_INJECT_ANNOTATIONS =
            new HashSet<Class<? extends Annotation>>() {{
                addAll(JaxRsParamProducer.JAX_RS_STRING_PARAM_ANNOTATIONS);
                add(Context.class);
            }};


    // Check first if a class is a JAX-RS resource, and only if so check with validation.
    // This prevents unnecessary warnings being logged for pure CDI beans.
    private final Cache<Class<?>, Boolean> jaxRsResourceCache = new Cache<>(
            clazz -> Resource.from(clazz, true) != null && Resource.from(clazz) != null);

    /**
     * CDI producer for CDI bean constructor String parameters, that should be injected by JAX-RS.
     */
    @ApplicationScoped
    public static class JaxRsParamProducer {

        @Qualifier
        @Retention(RUNTIME)
        @Target({METHOD, FIELD, PARAMETER, TYPE})
        public static @interface JaxRsParamQualifier {
        }

        private static final JaxRsParamQualifier JaxRsParamQUALIFIER = new JaxRsParamQualifier() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return JaxRsParamQualifier.class;
            }
        };

        static final Set<Class<? extends Annotation>> JAX_RS_STRING_PARAM_ANNOTATIONS =
                new HashSet<Class<? extends Annotation>>() {{
                    add(javax.ws.rs.PathParam.class);
                    add(javax.ws.rs.QueryParam.class);
                    add(javax.ws.rs.CookieParam.class);
                    add(javax.ws.rs.HeaderParam.class);
                    add(javax.ws.rs.MatrixParam.class);
                    add(javax.ws.rs.FormParam.class);
                }};

        /**
         * Internal cache to store CDI {@link InjectionPoint} to Jersey {@link Parameter} mapping.
         */
        final Cache<InjectionPoint, Parameter> parameterCache = new Cache<>(injectionPoint -> {
            final Annotated annotated = injectionPoint.getAnnotated();
            final Class<?> clazz = injectionPoint.getMember().getDeclaringClass();

            if (annotated instanceof AnnotatedParameter) {

                final AnnotatedParameter annotatedParameter = (AnnotatedParameter) annotated;
                final AnnotatedCallable callable = annotatedParameter.getDeclaringCallable();

                if (callable instanceof AnnotatedConstructor) {

                    final AnnotatedConstructor ac = (AnnotatedConstructor) callable;
                    final int position = annotatedParameter.getPosition();
                    final List<Parameter> parameters = Parameter.create(clazz, clazz, ac.getJavaMember(), false);

                    return parameters.get(position);
                }
            }

            return null;
        });

        /**
         * Provide a value for given injection point. If the injection point does not refer
         * to a CDI bean constructor parameter, or the value could not be found, the method will return null.
         *
         * @param injectionPoint actual injection point.
         * @param beanManager    current application bean manager.
         * @return concrete JAX-RS parameter value for given injection point.
         */
        @javax.enterprise.inject.Produces
        @JaxRsParamQualifier
        public String getParameterValue(final InjectionPoint injectionPoint, final BeanManager beanManager) {
            final Parameter parameter = parameterCache.apply(injectionPoint);

            if (parameter != null) {
                InjectionManager injectionManager =
                        beanManager.getExtension(CdiComponentProvider.class).getEffectiveInjectionManager();

                Set<ValueParamProvider> providers = Providers.getProviders(injectionManager, ValueParamProvider.class);
                ContainerRequest containerRequest = injectionManager.getInstance(ContainerRequest.class);
                for (ValueParamProvider vfp : providers) {
                    Function<ContainerRequest, ?> paramValueSupplier = vfp.getValueProvider(parameter);
                    if (paramValueSupplier != null) {
                        return (String) paramValueSupplier.apply(containerRequest);
                    }
                }
            }

            return null;
        }
    }

    @Override
    public AnnotatedParameter<?> getAnnotatedParameter(AnnotatedParameter<?> ap) {
        return new AnnotatedParameter() {

            @Override
            public int getPosition() {
                return ap.getPosition();
            }

            @Override
            public AnnotatedCallable getDeclaringCallable() {
                return ap.getDeclaringCallable();
            }

            @Override
            public Type getBaseType() {
                return ap.getBaseType();
            }

            @Override
            public Set<Type> getTypeClosure() {
                return ap.getTypeClosure();
            }

            @Override
            public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
                if (annotationType == JaxRsParamProducer.JaxRsParamQualifier.class) {
                    return CdiComponentProvider.hasAnnotation(ap, JaxRsParamProducer.JAX_RS_STRING_PARAM_ANNOTATIONS)
                            ? (T) JaxRsParamProducer.JaxRsParamQUALIFIER : null;
                } else {
                    return ap.getAnnotation(annotationType);
                }
            }

            @Override
            public Set<Annotation> getAnnotations() {
                final Set<Annotation> result = new HashSet<>();
                for (final Annotation a : ap.getAnnotations()) {
                    result.add(a);
                    final Class<? extends Annotation> annotationType = a.annotationType();
                    if (JaxRsParamProducer.JAX_RS_STRING_PARAM_ANNOTATIONS.contains(annotationType)) {
                        result.add(JaxRsParamProducer.JaxRsParamQUALIFIER);
                    }
                }
                return result;
            }

            @Override
            public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
                return (annotationType == JaxRsParamProducer.JaxRsParamQualifier.class
                        && CdiComponentProvider.hasAnnotation(ap, JaxRsParamProducer.JAX_RS_STRING_PARAM_ANNOTATIONS))
                        || ap.isAnnotationPresent(annotationType);
            }
        };
    }

    @Override
    public Set<Class<? extends Annotation>> getJaxRsInjectAnnotations() {
        return JAX_RS_INJECT_ANNOTATIONS;
    }

    @Override
    public boolean isAcceptableResource(Class<?> resource) {
        return Resource.isAcceptable(resource);
    }

    @Override
    public boolean isJaxRsResource(Class<?> resource) {
        return jaxRsResourceCache.apply(resource);
    }

    @Override
    public boolean containsJaxRsParameterizedCtor(final AnnotatedType annotatedType) {
        return CdiComponentProvider
                .containAnnotatedParameters(annotatedType.getConstructors(), JaxRsParamProducer.JAX_RS_STRING_PARAM_ANNOTATIONS);
    }
}
