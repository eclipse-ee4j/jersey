/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.sse.Sse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
public class InjectExtension implements Extension {
    private static final Class<?> WEB_CONFIG_CLASS =
            AccessController.doPrivileged(ReflectionHelper.classForNamePA("org.glassfish.jersey.servlet.WebConfig"));
    private AnnotatedType<ServletReferenceProducer> interceptorAnnotatedType;

    private void processAnnotatedType(@Observes ProcessAnnotatedType<?> processAnnotatedType,
                                      BeanManager beanManager) {
        final Class<?> baseClass = (Class<?>) processAnnotatedType.getAnnotatedType().getBaseType();
        if (Application.class.isAssignableFrom(baseClass) && Configuration.class.isAssignableFrom(baseClass)) {
            if (!baseClass.isAnnotationPresent(Alternative.class)) {
                processAnnotatedType.veto(); // Filter bean annotated ResourceConfig
            }
        }
    }

    private void beforeDiscoveryObserver(@Observes final BeforeBeanDiscovery bbf, final BeanManager beanManager) {
        if (WEB_CONFIG_CLASS != null) {
            interceptorAnnotatedType = beanManager.createAnnotatedType(ServletReferenceProducer.class);
            bbf.addAnnotatedType(interceptorAnnotatedType, ServletReferenceProducer.class.getName());
        }
        CdiComponentProvider.addHK2DepenendencyCheck(InjectExtension::isHK2Dependency);
    }

    private void afterDiscoveryObserver(@Observes final AfterBeanDiscovery abd, final BeanManager beanManager) {
        if (WEB_CONFIG_CLASS != null) {
            abd.addBean(new ServletReferenceProducerBean(beanManager));
        }
    }

    @Singleton
    private final class ServletReferenceProducerBean implements Bean<ServletReferenceProducer>, PassivationCapable {
        private final Set<Annotation> qualifiers = new HashSet<>();
        private final Set<Type> types = new HashSet<>(2);
        private final InjectionTarget<ServletReferenceProducer> interceptorTarget;
        private final String id = UUID.randomUUID().toString();

        private ServletReferenceProducerBean(BeanManager beanManager) {
            qualifiers.add(new CdiJerseyContextAnnotation());
            qualifiers.add(new CdiAnyAnnotation());

            types.add(ServletReferenceProducer.class);
            types.add(Object.class);

            final AnnotatedType<ServletReferenceProducer> interceptorType = interceptorAnnotatedType;
            final InjectionTargetFactory<ServletReferenceProducer> injectionTargetFactory =
                    beanManager.getInjectionTargetFactory(interceptorType);

            interceptorTarget = injectionTargetFactory.createInjectionTarget(null);
        }
        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return RequestScoped.class;
        }

        @Override
        public String getName() {
            return ServletReferenceProducer.class.getName();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public ServletReferenceProducer create(CreationalContext<ServletReferenceProducer> creationalContext) {
            final ServletReferenceProducer result = interceptorTarget.produce(creationalContext);
            interceptorTarget.inject(result, creationalContext);
            interceptorTarget.postConstruct(result);
            return result;
        }

        @Override
        public void destroy(ServletReferenceProducer servletProducer,
                            CreationalContext<ServletReferenceProducer> creationalContext) {
            interceptorTarget.preDestroy(servletProducer);
            interceptorTarget.dispose(servletProducer);
            creationalContext.release();
        }

        @Override
        public Class<?> getBeanClass() {
            return ServletReferenceProducer.class;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return interceptorTarget.getInjectionPoints();
        }

        public boolean isNullable() {
            return false;
        }

        @Override
        public String getId() {
            return id;
        }
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

        return injectables;
    }

    private static class CdiJerseyContextAnnotation
            extends jakarta.enterprise.util.AnnotationLiteral<JerseyContext> implements JerseyContext {
        private static final long serialVersionUID = 1L;
    }

    private static class CdiAnyAnnotation
            extends jakarta.enterprise.util.AnnotationLiteral<Any> implements Any {
        private static final long serialVersionUID = 1L;
    }

}
