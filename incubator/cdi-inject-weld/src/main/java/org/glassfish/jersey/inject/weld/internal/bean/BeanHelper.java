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

package org.glassfish.jersey.inject.weld.internal.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.data.BindingBeanPair;
import org.glassfish.jersey.inject.weld.internal.inject.InitializableInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.inject.InitializableSupplierInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.injector.CachedConstructorAnalyzer;
import org.glassfish.jersey.inject.weld.internal.injector.InjectionUtils;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyConstructorInjectionPoint;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyInjectionTarget;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyTwofoldInstantiator;
import org.glassfish.jersey.inject.weld.internal.injector.WrappingJerseyInjectionTarget;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.internal.inject.SupplierClassBinding;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.jlr.ConstructorSignatureImpl;
import org.jboss.weld.annotated.enhanced.jlr.EnhancedAnnotatedTypeImpl;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.NonProducibleInjectionTarget;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Helper class to register a {@link Bean} into CDI {@link BeanManager}.
 */
public abstract class BeanHelper {

    /**
     * Forbids the creation of {@link BeanHelper} instance.
     */
    private BeanHelper() {
    }

    /**
     * Registers an instance as {@link JerseyBean} into {@link BeanManager}.
     *
     * @param binding   object containing {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
     * @param abd       {@link AfterBeanDiscovery} event.
     * @param resolvers all registered injection resolvers.
     * @param <T>       type of the instance which is registered.
     */
    public static <T> void registerBean(RuntimeType runtimeType, InitializableInstanceBinding<T> binding, AfterBeanDiscovery abd,
                                        List<InjectionResolver> resolvers, BeanManager beanManager) {
        InitializableInstanceBean<T> bean = new InitializableInstanceBean<>(runtimeType, binding);
        /*
         * Wrap into custom injection target that is able to inject the additional @Inject, @Context, @*Param fields into
         * the given service.
         */
        InjectionTarget<T> injectionTarget = new WrappingJerseyInjectionTarget<>(bean, resolvers);
        bean.setInjectionTarget(injectionTarget);
        abd.addBean(bean);
    }

    /**
     * Registers a class as {@link JerseyBean} into {@link BeanManager}.
     *
     * @param binding     object containing {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
     * @param abd         {@link AfterBeanDiscovery} event.
     * @param resolvers   all registered injection resolvers.
     * @param beanManager currently used bean manager.
     * @param <T>         type of the class which is registered.
     */
    public static <T> BindingBeanPair registerBean(RuntimeType runtimeType, ClassBinding<T> binding, AfterBeanDiscovery abd,
                                                   Collection<InjectionResolver> resolvers, BeanManager beanManager) {
        AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(binding.getService());
        InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        InjectionTarget<T> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        ClassBean<T> bean = new ClassBean<>(runtimeType, binding);
        bean.setInjectionTarget(getJerseyInjectionTarget(binding.getService(), injectionTarget, bean, resolvers));
        abd.addBean(bean);

        return new BindingBeanPair(binding, bean);
    }

    /**
     * Registers an instance supplier and its provided value as {@link JerseyBean}s into {@link BeanManager}.
     *
     * @param binding object containing {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
     * @param abd     {@link AfterBeanDiscovery} event.
     * @param <T>     type of the instance which is registered.
     */
    public static <T> void registerSupplier(RuntimeType runtimeType, InitializableSupplierInstanceBinding<T> binding,
                                            AfterBeanDiscovery abd, BeanManager beanManager) {
        /*
         * CDI does not provide sufficient support for ThreadScoped Supplier
         */
        if (binding.getScope() == PerThread.class) {
            abd.addBean(new InitializableSupplierThreadScopeBean(runtimeType, binding, beanManagerImpl(beanManager)));
        } else {
            abd.addBean(new InitializableSupplierInstanceBean<>(runtimeType, binding));
            abd.addBean(new InitializableSupplierInstanceBeanBridge<>(runtimeType, binding));
        }
    }

    /**
     * Registers a class supplier and its provided value as {@link JerseyBean}s into {@link BeanManager}.
     *
     * @param binding     object containing {@link jakarta.enterprise.inject.spi.BeanAttributes} information.
     * @param abd         {@link AfterBeanDiscovery} event.
     * @param resolvers   all registered injection resolvers.
     * @param beanManager currently used bean manager.
     * @param <T>         type of the class which is registered.
     */
    @SuppressWarnings("unchecked")
    public static <T> BindingBeanPair registerSupplier(RuntimeType runtimeType, SupplierClassBinding<T> binding,
            AfterBeanDiscovery abd, Collection<InjectionResolver> resolvers, BeanManager beanManager) {

        final Class<Supplier<T>> supplierClass = (Class<Supplier<T>>) binding.getSupplierClass();
        final AnnotatedType<Supplier<T>> annotatedType = beanManager.createAnnotatedType(supplierClass);
        final InjectionTargetFactory<Supplier<T>> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        final InjectionTarget<Supplier<T>> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        SupplierClassBean<T> supplierBean = new SupplierClassBean<>(runtimeType, binding);
        InjectionTarget<Supplier<T>> jit = getJerseyInjectionTarget(supplierClass, injectionTarget, supplierBean, resolvers);
        supplierBean.setInjectionTarget(jit);

        /*
         * CDI does not provide sufficient support for ThreadScoped Supplier
         */
        if (binding.getScope() == PerThread.class) {
            abd.addBean(new SupplierThreadScopeClassBean(runtimeType, binding, supplierBean, beanManagerImpl(beanManager)));
            return null;
        } else {
            final SupplierBeanBridge supplierBeanBridge = new SupplierBeanBridge(runtimeType, binding, beanManager);
            abd.addBean(supplierBean);
            abd.addBean(supplierBeanBridge);
            return new BindingBeanPair(binding, supplierBean, supplierBeanBridge);
        }
    }

    /**
     * Update ClassBinding Bean by {@link ConstructorInjectionPoint} for the client side beans.
     * @param binding The ClassBinding used to create a client side ConstructorInjectionPoint.
     * @param pair {@link BindingBeanPair} that contains the original server side Bean.
     * @param resolvers Resolvers handling Jersey specific injection annotations.
     * @param beanManager The {@link BeanManager}.
     */
    public static void updateBean(ClassBinding binding,
            BindingBeanPair pair, Collection<InjectionResolver> resolvers, BeanManager beanManager) {

        final JerseyBean bean = pair.getBeans().get(0);
        final ConstructorInjectionPoint cip = createConstructorInjectionPoint(binding, bean, resolvers, beanManager);

        if (ClassBean.class.isInstance(bean)
                && JerseyInjectionTarget.class.isInstance(((ClassBean) bean).getInjectionTarget())) {
            final JerseyTwofoldInstantiator instantiator =
                    ((JerseyInjectionTarget) ((ClassBean) bean).getInjectionTarget()).getTwofoldInstantiator();
            instantiator.setOptionalConstructorInjectionPoint(cip);
        }
    }

    /**
     * Update SupplierClassBinding Bean by {@link ConstructorInjectionPoint} for the client side beans.
     * @param binding The SupplierClassBinding used to create a client side ConstructorInjectionPoint.
     * @param pair {@link BindingBeanPair} that contains the original server side Bean.
     * @param resolvers Resolvers handling Jersey specific injection annotations.
     * @param beanManager The {@link BeanManager}.
     */
    public static void updateSupplierBean(SupplierClassBinding binding,
            BindingBeanPair pair, Collection<InjectionResolver> resolvers, BeanManager beanManager) {

        final JerseyBean bean = pair.getBeans().get(0);
        final ConstructorInjectionPoint cip = createConstructorInjectionPoint(binding, bean, resolvers, beanManager);

        if (SupplierClassBean.class.isInstance(bean)
                && JerseyInjectionTarget.class.isInstance(((SupplierClassBean) bean).getInjectionTarget())) {
            final JerseyTwofoldInstantiator instantiator =
                    ((JerseyInjectionTarget) ((SupplierClassBean) bean).getInjectionTarget()).getTwofoldInstantiator();
            instantiator.setOptionalConstructorInjectionPoint(cip);
        }
    }

    private static <T> ConstructorInjectionPoint<T> createConstructorInjectionPoint(
            SupplierClassBinding<T> binding, Bean<T> bean, Collection<InjectionResolver> resolvers, BeanManager beanManager) {

        final Class<Supplier<T>> bindingClass = (Class<Supplier<T>>) binding.getSupplierClass();
        final AnnotatedType<Supplier<T>> annotatedType = beanManager.createAnnotatedType(bindingClass);
        final InjectionTargetFactory<Supplier<T>> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        final InjectionTarget<Supplier<T>> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        final CachedConstructorAnalyzer<Supplier<T>> analyzer =
                new CachedConstructorAnalyzer<>(bindingClass, InjectionUtils.getInjectAnnotations(resolvers));

        if (analyzer.hasCompatibleConstructor()) {
            EnhancedAnnotatedConstructor<T> constructor = createEnhancedAnnotatedType((BasicInjectionTarget) injectionTarget)
                    .getDeclaredEnhancedConstructor(new ConstructorSignatureImpl(analyzer.getConstructor()));

            JerseyConstructorInjectionPoint<T> constructorInjectionPoint = new JerseyConstructorInjectionPoint<T>(
                    constructor, bean, ((BasicInjectionTarget) injectionTarget).getBeanManager(), resolvers);
            return constructorInjectionPoint;
        }
        return null;
    }

    private static <T> ConstructorInjectionPoint<T> createConstructorInjectionPoint(
            ClassBinding<T> binding, Bean<T> bean, Collection<InjectionResolver> resolvers, BeanManager beanManager) {

        final Class<T> bindingClass = binding.getImplementationType();
        final AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(bindingClass);
        final InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        final InjectionTarget<T> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        final CachedConstructorAnalyzer<T> analyzer =
                new CachedConstructorAnalyzer<>(bindingClass, InjectionUtils.getInjectAnnotations(resolvers));

        if (analyzer.hasCompatibleConstructor()) {
            EnhancedAnnotatedConstructor<T> constructor = createEnhancedAnnotatedType((BasicInjectionTarget) injectionTarget)
                    .getDeclaredEnhancedConstructor(new ConstructorSignatureImpl(analyzer.getConstructor()));

            JerseyConstructorInjectionPoint<T> constructorInjectionPoint = new JerseyConstructorInjectionPoint<T>(
                    constructor, bean, ((BasicInjectionTarget) injectionTarget).getBeanManager(), resolvers);
            return constructorInjectionPoint;
        }
        return null;
    }

    private static BeanManagerImpl beanManagerImpl(BeanManager beanManager) {
        if (beanManager instanceof BeanManagerProxy) {
            return ((BeanManagerProxy) beanManager).unwrap();
        } else {
            return (BeanManagerImpl) beanManager;
        }
    }

    private static <T> InjectionTarget<T> getJerseyInjectionTarget(Class<T> clazz, InjectionTarget<T> injectionTarget,
            Bean<T> bean, Collection<InjectionResolver> resolvers) {
        BasicInjectionTarget<T> it = (BasicInjectionTarget<T>) injectionTarget;

        /*
         * Looks at whether the DefaultInstantiator resolving a valid constructor does not met this case:
         * - No constructor with @Inject annotation is defined
         * - NoArgs constructor is defined
         * - Instantiator ignores JAX-RS valid constructor with multiple params
         */
        boolean noArgConstructor = isNoArgConstructorCase(it, clazz);

        JerseyInjectionTarget<T> jit;
        /*
         * CDI is able to find a constructor that means that the class contains only one constructor of this type:
         * - default constructor
         * - non-argument constructor
         * - multi-param constructor annotated by @Inject annotation and able to inject all parameters.
         */
        if (!noArgConstructor && injectionTarget instanceof BeanInjectionTarget) {
            jit = new JerseyInjectionTarget<>(it, bean, clazz, resolvers);

        /*
         * CDI failed during the looking for a proper constructor because of these reasons:
         * - multi-param constructor not annotated by @Inject annotation
         * - multiple constructors annotated by @Inject annotation
         * - is not able to satisfied single constructor annotated by @Inject annotation
         *
         * Therefore produced NonProducibleInjectionTarget cannot create and instance, we try to find the proper constructor
         * using JAX-RS rules:
         * - largest constructor with all annotated parameters
         *
         * If JAX-RS valid constructor is not find - InjectionException is thrown
         */
        } else if (noArgConstructor || injectionTarget instanceof NonProducibleInjectionTarget) {
            CachedConstructorAnalyzer<T> analyzer =
                    new CachedConstructorAnalyzer<>(clazz, InjectionUtils.getInjectAnnotations(resolvers));

            /*
             * Contains the analyzed class any constructor that can be injected by Jersey?
             */
            if (analyzer.hasCompatibleConstructor()) {
                EnhancedAnnotatedConstructor<T> constructor = createEnhancedAnnotatedType(it)
                        .getDeclaredEnhancedConstructor(new ConstructorSignatureImpl(analyzer.getConstructor()));

                JerseyConstructorInjectionPoint<T> constructorInjectionPoint =
                        new JerseyConstructorInjectionPoint<>(constructor, bean, it.getBeanManager(), resolvers);

                Instantiator<T> instantiator = new JerseyInstantiator<>(constructorInjectionPoint);
                jit = new JerseyInjectionTarget<>(createEnhancedAnnotatedType(it), it, bean, clazz, resolvers, instantiator);

            /*
             * Instance of this class cannot be created neither CDI nor Jersey therefore mark it as non-producible.
             */
            } else {
                return new WrappingJerseyInjectionTarget<>(it, bean, resolvers);
            }
        } else {
            throw new RuntimeException("Unknown InjectionTarget for the class: " + clazz.getTypeName());
        }

        InjectionTargetService injectionTargetService = it.getBeanManager().getServices().get(InjectionTargetService.class);
        injectionTargetService.addInjectionTargetToBeInitialized(jit.getEnhancedAnnotatedType(), jit);
        return jit;
    }

    public static <T> EnhancedAnnotatedType<T> createEnhancedAnnotatedType(BasicInjectionTarget<T> it) {
        return EnhancedAnnotatedTypeImpl.of(
                (SlimAnnotatedType<T>) it.getAnnotatedType(), ClassTransformer.instance(it.getBeanManager()));
    }

    /**
     * Looks at whether the DefaultInstantiator resolving a valid constructor does not met this case:
     * - No constructor with @Inject annotation is defined
     * - NoArgs constructor is defined
     * - Instantiator ignores JAX-RS valid constructor with multiple params
     *
     * @param it    injection target containing instantiator with resolved constructor.
     * @param clazz class which analyzed constructor belongs to.
     * @param <T>   type of the analyzed class.
     * @return {@code true} if no-arg constructor was selected while multi-params constructor exists.
     */
    private static <T> boolean isNoArgConstructorCase(BasicInjectionTarget<T> it, Class<T> clazz) {
        if (!(it instanceof NonProducibleInjectionTarget)) {
            Instantiator<T> instantiator = it.getInstantiator();
            Constructor<T> constructor = instantiator.getConstructor();
            return constructor.getParameterCount() == 0 && clazz.getConstructors().length > 1;
        }

        return false;
    }

    /**
     * Wrapper class to provide Jersey implementation of {@link Instantiator} interface.
     *
     * @param <T> type which is created by instantiator.
     */
    private static class JerseyInstantiator<T> extends AbstractInstantiator<T> {

        private final ConstructorInjectionPoint<T> injectionPoint;

        private JerseyInstantiator(ConstructorInjectionPoint<T> injectionPoint) {
            this.injectionPoint = injectionPoint;
        }

        @Override
        public ConstructorInjectionPoint<T> getConstructorInjectionPoint() {
            return injectionPoint;
        }

        @Override
        public Constructor<T> getConstructor() {
            return injectionPoint.getAnnotated().getJavaMember();
        }

        @Override
        public String toString() {
            return "JerseyInstantiator [constructor=" + injectionPoint.getMember() + "]";
        }

        @Override
        public boolean hasInterceptorSupport() {
            return false;
        }

        @Override
        public boolean hasDecoratorSupport() {
            return false;
        }
    }

    public static boolean isResourceClass(Class<?> clazz) {
        if (isJaxrsResource(clazz)) {
            return true;
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            if (isJaxrsResource(iface)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isJaxrsResource(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Path.class)) {
            return true;
        }

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                return true;
            }

            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
                    return true;
                }
            }
        }

        return false;
    }
}
