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

import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.hibernate.validator.cdi.internal.interceptor.MethodValidated;
import org.hibernate.validator.cdi.internal.interceptor.ValidationInterceptor;

/**
 * JAX-RS wrapper for Hibernate CDI bean validation interceptor.
 * Since Jersey already executes validation on JAX-RS resources,
 * Jersey registers this wrapper into CDI container so that JAX-RS
 * components do not get validated twice.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@MethodValidated
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_AFTER + 800)
public class CdiInterceptorWrapper {

    private final ValidationInterceptor interceptor;

    @Inject
    public CdiInterceptorWrapper(BeanManager beanManager) {
        // get the original interceptor from the bean manager directly
        // to avoid CDI bootstrap issues caused by wrong extension ordering
        final Set<Bean<?>> interceptorBeans = beanManager.getBeans(ValidationInterceptor.class);
        final Bean<?> interceptorBean = beanManager.resolve(interceptorBeans);
        this.interceptor = (ValidationInterceptor) beanManager.getReference(
                interceptorBean, ValidationInterceptor.class, beanManager.createCreationalContext(interceptorBean));
    }

    @Inject
    private CdiInterceptorWrapperExtension extension;

    @AroundInvoke
    public Object validateMethodInvocation(InvocationContext ctx) throws Exception {
        final boolean isJaxRsMethod = extension.jaxRsResourceCache.apply(ctx.getMethod().getDeclaringClass());
        return isJaxRsMethod ? ctx.proceed() : interceptor.validateMethodInvocation(ctx);
    }

    @AroundConstruct
    public void validateConstructorInvocation(InvocationContext ctx) throws Exception {
        final boolean isJaxRsConstructor = extension.jaxRsResourceCache.apply(ctx.getConstructor().getDeclaringClass());
        if (!isJaxRsConstructor) {
            interceptor.validateConstructorInvocation(ctx);
        }
    }
}
