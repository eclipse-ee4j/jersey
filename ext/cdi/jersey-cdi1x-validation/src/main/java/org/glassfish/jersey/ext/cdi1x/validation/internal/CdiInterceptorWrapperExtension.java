/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 * Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.validation.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.interceptor.Interceptor;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;
import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.server.model.Resource;

import org.hibernate.validator.cdi.interceptor.spi.ValidationInterceptor;

/**
 * CDI extension to register {@link CdiInterceptorWrapper}.
 *
 * @author Jakub Podlesak
 */
@Priority(value = Interceptor.Priority.PLATFORM_BEFORE + 199)
@JerseyVetoed
public class CdiInterceptorWrapperExtension implements Extension {

    public static final AnnotationLiteral<Default> DEFAULT_ANNOTATION_LITERAL = new AnnotationLiteral<Default>() {};
    public static final AnnotationLiteral<Any> ANY_ANNOTATION_LITERAL = new AnnotationLiteral<Any>() {};

    private Cache<Class<?>, Boolean> jaxRsResourceCache = new Cache<>(clazz -> Resource.from(clazz) != null);

    private AnnotatedType<ValidationInterceptor> interceptorAnnotatedType;

    /**
     * Register our validation interceptor wrapper.
     *
     * @param beforeBeanDiscoveryEvent CDI bootstrap event.
     * @param beanManager current bean manager.
     */
    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
        beforeBeanDiscoveryEvent.addAnnotatedType(beanManager.createAnnotatedType(CdiInterceptorWrapper.class),
                "Jersey " + CdiInterceptorWrapper.class.getName()
        );
        interceptorAnnotatedType = beanManager.createAnnotatedType(ValidationInterceptor.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(interceptorAnnotatedType,
                "Jersey " + ValidationInterceptor.class.getName()
        );
    }

    /**
     * Remove the original interceptor, as we are going to proxy the calls with {@link CdiInterceptorWrapper}.
     *
     * @param afterTypeDiscovery CDI bootstrap event.
     */
    private void afterTypeDiscovery(@Observes final AfterTypeDiscovery afterTypeDiscovery) {
        // Does throw java.lang.IndexOutOfBoundsException in Weld 4
        // afterTypeDiscovery.getInterceptors().removeIf(ValidationInterceptor.class::equals);
        // iterator.remove throws as well
        afterTypeDiscovery.getInterceptors().remove(ValidationInterceptor.class);
    }

    /**
     * Register a CDI bean for the original interceptor so that we can have it injected in our wrapper.
     *
     * @param afterBeanDiscovery CDI bootstrap event.
     * @param beanManager current bean manager
     */
    private void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        // we need the injection target so that CDI could instantiate the original interceptor for us
        final AnnotatedType<ValidationInterceptor> interceptorType = interceptorAnnotatedType;
        final InjectionTargetFactory<ValidationInterceptor> injectionTargetFactory =
                beanManager.getInjectionTargetFactory(interceptorType);
        final InjectionTarget<ValidationInterceptor> interceptorTarget =
                injectionTargetFactory.createInjectionTarget(null);


        afterBeanDiscovery.addBean(new Bean<ValidationInterceptor>() {

            @Override
            public Class<?> getBeanClass() {
                return ValidationInterceptor.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return interceptorTarget.getInjectionPoints();
            }

            @Override
            public String getName() {
                return "HibernateValidationInterceptorImpl";
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return new HashSet<Annotation>() {{
                    add(DEFAULT_ANNOTATION_LITERAL);
                    add(ANY_ANNOTATION_LITERAL);
                }};
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public Set<Type> getTypes() {
                return new HashSet<Type>() {{
                    add(ValidationInterceptor.class);
                    add(Object.class);
                }};
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            // @Override - Removed in CDI 4
            public boolean isNullable() {
                return false;
            }

            @Override
            public ValidationInterceptor create(CreationalContext<ValidationInterceptor> ctx) {

                final ValidationInterceptor result = interceptorTarget.produce(ctx);
                interceptorTarget.inject(result, ctx);
                interceptorTarget.postConstruct(result);
                return result;
            }


            @Override
            public void destroy(ValidationInterceptor instance,
                                CreationalContext<ValidationInterceptor> ctx) {

                interceptorTarget.preDestroy(instance);
                interceptorTarget.dispose(instance);
                ctx.release();
            }
        });
    }

    /* package */ Cache<Class<?>, Boolean> getJaxRsResourceCache() {
        return jaxRsResourceCache;
    }
}
