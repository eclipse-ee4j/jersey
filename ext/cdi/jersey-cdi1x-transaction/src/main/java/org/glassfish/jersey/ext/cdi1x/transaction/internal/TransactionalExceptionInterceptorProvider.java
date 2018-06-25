/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.transaction.internal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Qualifier;
import javax.interceptor.Interceptor;
import javax.transaction.TransactionalException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.ext.cdi1x.internal.CdiUtil;
import org.glassfish.jersey.ext.cdi1x.internal.GenericCdiBeanSupplier;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

/**
 * Jersey CDI extension that provides means to retain {@link WebApplicationException}
 * thrown from JAX-RS components implemented as CDI transactional beans.
 * This is to avoid the {@link WebApplicationException} from being masked with
 * {@link TransactionalException}. Jersey will try to restore the original
 * JAX-RS exception using {@link TransactionalExceptionMapper}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Priority(value = Interceptor.Priority.PLATFORM_BEFORE + 199)
public class TransactionalExceptionInterceptorProvider implements ComponentProvider, Extension {

    private InjectionManager injectionManager;
    private BeanManager beanManager;

    @Qualifier
    @Retention(RUNTIME)
    @Target({METHOD, FIELD, PARAMETER, TYPE})
    public static @interface WaeQualifier {
    }

    @Override
    public void initialize(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        this.beanManager = CdiUtil.getBeanManager();
    }

    @Override
    public boolean bind(final Class<?> component, final Set<Class<?>> providerContracts) {
        return false;
    }

    @Override
    public void done() {
        if (beanManager != null) {
            bindWaeRestoringExceptionMapper();
        }
    }

    private void bindWaeRestoringExceptionMapper() {
        GenericCdiBeanSupplier beanSupplier =
                new GenericCdiBeanSupplier(TransactionalExceptionMapper.class, injectionManager, beanManager, true);
        Binding binding = Bindings.supplier(beanSupplier).to(ExceptionMapper.class);
        injectionManager.register(binding);
    }

    @SuppressWarnings("unused")
    private void afterTypeDiscovery(@Observes final AfterTypeDiscovery afterTypeDiscovery) {
        final List<Class<?>> interceptors = afterTypeDiscovery.getInterceptors();
        interceptors.add(WebAppExceptionInterceptor.class);
    }

    @SuppressWarnings("unused")
    private void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager,
                WebAppExceptionHolder.class,
                WebAppExceptionInterceptor.class,
                TransactionalExceptionMapper.class
        );
    }

    private static void addAnnotatedTypes(BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager, Class<?>... types) {
        for (Class<?> type : types) {
            beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(type), "Jersey " + type.getName());
        }
    }
}
