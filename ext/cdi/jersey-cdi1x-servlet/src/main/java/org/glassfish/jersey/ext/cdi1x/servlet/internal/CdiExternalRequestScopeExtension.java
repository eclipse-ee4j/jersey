/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.ext.cdi1x.servlet.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

/**
 * CDI extension to register {@link CdiExternalRequestScope}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class CdiExternalRequestScopeExtension implements Extension {

    public static final AnnotationLiteral<Default> DEFAULT_ANNOTATION_LITERAL = new AnnotationLiteral<Default>() {};
    public static final AnnotationLiteral<Any> ANY_ANNOTATION_LITERAL = new AnnotationLiteral<Any>() {};

    private AnnotatedType<CdiExternalRequestScope> requestScopeType;

    /**
     * Register our external request scope.
     *
     * @param beforeBeanDiscoveryEvent CDI bootstrap event.
     * @param beanManager current bean manager.
     */
    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent, final BeanManager beanManager) {
        requestScopeType = beanManager.createAnnotatedType(CdiExternalRequestScope.class);
        beforeBeanDiscoveryEvent.addAnnotatedType(requestScopeType, "Jersey " + CdiExternalRequestScope.class.getName());
    }

    /**
     * Register a CDI bean for the external scope.
     *
     * @param afterBeanDiscovery CDI bootstrap event.
     * @param beanManager current bean manager
     */
    private void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        // we need the injection target so that CDI could instantiate the original interceptor for us
        final InjectionTarget<CdiExternalRequestScope> interceptorTarget = beanManager.createInjectionTarget(requestScopeType);


        afterBeanDiscovery.addBean(new Bean<CdiExternalRequestScope>() {

            @Override
            public Class<?> getBeanClass() {
                return CdiExternalRequestScope.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return interceptorTarget.getInjectionPoints();
            }

            @Override
            public String getName() {
                return "CdiExternalRequestScope";
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
                    add(CdiExternalRequestScope.class);
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
            public CdiExternalRequestScope create(CreationalContext<CdiExternalRequestScope> ctx) {

                final CdiExternalRequestScope result = interceptorTarget.produce(ctx);
                interceptorTarget.inject(result, ctx);
                interceptorTarget.postConstruct(result);
                return result;
            }


            @Override
            public void destroy(CdiExternalRequestScope instance,
                                CreationalContext<CdiExternalRequestScope> ctx) {

                interceptorTarget.preDestroy(instance);
                interceptorTarget.dispose(instance);
                ctx.release();
            }
        });
    }
}
