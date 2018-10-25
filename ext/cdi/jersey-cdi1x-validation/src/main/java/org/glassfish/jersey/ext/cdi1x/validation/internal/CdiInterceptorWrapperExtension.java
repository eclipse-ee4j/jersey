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

package org.glassfish.jersey.ext.cdi1x.validation.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.Interceptor;

import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.server.model.Resource;

import org.hibernate.validator.cdi.internal.interceptor.ValidationInterceptor;

/**
 * CDI extension to register {@link CdiInterceptorWrapper}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Priority(value = Interceptor.Priority.PLATFORM_BEFORE + 199)
public class CdiInterceptorWrapperExtension implements Extension {

    public static final AnnotationLiteral<Default> DEFAULT_ANNOTATION_LITERAL = new AnnotationLiteral<Default>() {};
    public static final AnnotationLiteral<Any> ANY_ANNOTATION_LITERAL = new AnnotationLiteral<Any>() {};

    final Cache<Class<?>, Boolean> jaxRsResourceCache = new Cache<>(clazz -> Resource.from(clazz) != null);

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
        afterTypeDiscovery.getInterceptors().removeIf(ValidationInterceptor.class::equals);
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
        final InjectionTarget<ValidationInterceptor> interceptorTarget = beanManager.createInjectionTarget(interceptorType);


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

            @Override
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
}
