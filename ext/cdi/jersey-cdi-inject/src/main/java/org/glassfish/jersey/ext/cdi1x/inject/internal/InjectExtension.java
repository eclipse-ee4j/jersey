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

package org.glassfish.jersey.ext.cdi1x.inject.internal;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.sse.Sse;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A utility class that makes sure {@code @Inject} can be used instead of {@code @Context} for the Jakarta REST API classes
 * and interfaces, such as for {@code Configuration}, or {@code Providers}.
 * </p>
 * <p>
 * Note that {@code ContextResolver} can be injected using {@code @Context}, but the Jakarta REST specification does not require
 * the implementation to be capable of doing so. Since {@code ContextResolver} is parametrized type, the injection using CDI's
 * {@Inject} is not supported. The {@code ContextResolver} can be obtained from {@code Providers}.
 * </p>
 */
@SuppressWarnings("unused")
class InjectExtension implements Extension {
    private void processAnnotatedType(@Observes ProcessAnnotatedType<?> processAnnotatedType, BeanManager beanManager) {
        final Class<?> baseClass = (Class<?>) processAnnotatedType.getAnnotatedType().getBaseType();
        if (Application.class.isAssignableFrom(baseClass) && Configuration.class.isAssignableFrom(baseClass)) {
            if (!baseClass.isAnnotationPresent(Alternative.class)) {
                processAnnotatedType.veto(); // Filter bean annotated ResourceConfig
            }
        }
    }

    private void beforeDiscoveryObserver(@Observes final BeforeBeanDiscovery bbf, final BeanManager beanManager) {
        final CdiComponentProvider cdiComponentProvider = beanManager.getExtension(CdiComponentProvider.class);
        cdiComponentProvider.addHK2DepenendencyCheck(InjectExtension::isHK2Dependency);
    }

    private static final boolean isHK2Dependency(Class<?> clazz) {
        return JERSEY_BOUND_INJECTABLES.get().contains(clazz);
    }

    private static final LazyValue<Set<Class<?>>> JERSEY_BOUND_INJECTABLES
            = Values.lazy((Value<Set<Class<?>>>) () -> sumNonJerseyBoundInjectables());

    private static Set<Class<?>> sumNonJerseyBoundInjectables() {
        final Set<Class<?>> injectables = new HashSet<>();

        //JAX-RS
        injectables.add(Application.class);
        injectables.add(Configuration.class);
        injectables.add(ContainerRequestContext.class);
        injectables.add(HttpHeaders.class);
        injectables.add(ParamConverterProvider.class);
        injectables.add(Providers.class);
        injectables.add(Request.class);
        injectables.add(ResourceContext.class);
        injectables.add(ResourceInfo.class);
        injectables.add(SecurityContext.class);
        injectables.add(Sse.class);
        injectables.add(UriInfo.class);

        //Servlet if available
        addOptionally("javax.servlet.http.HttpServletRequest", injectables);
        addOptionally("javax.servlet.http.HttpServletResponse", injectables);
        addOptionally("javax.servlet.ServletConfig", injectables);
        addOptionally("javax.servlet.ServletContext", injectables);
        addOptionally("javax.servlet.FilterConfig", injectables);

        return injectables;
    }

    private static void addOptionally(String className, Set<Class<?>> set) {
        final Class<?> optionalClass = AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
        if (optionalClass != null) {
            set.add(optionalClass);
        }
    }
}
