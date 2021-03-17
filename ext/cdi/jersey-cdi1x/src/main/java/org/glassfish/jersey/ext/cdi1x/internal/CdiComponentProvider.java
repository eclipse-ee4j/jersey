/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.ws.rs.core.Application;

import javax.annotation.ManagedBean;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerInjectedTarget;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionTargetListener;
import org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;
import org.glassfish.jersey.internal.inject.ForeignRequestScopeBridge;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;
import org.glassfish.jersey.internal.util.collection.Cache;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.spi.ComponentProvider;

import org.glassfish.hk2.api.ClassAnalyzer;

/**
 * Jersey CDI integration implementation.
 * Implements {@link ComponentProvider Jersey component provider} to serve CDI beans
 * obtained from the actual CDI bean manager.
 * To properly inject JAX-RS/Jersey managed beans into CDI, it also
 * serves as a {@link Extension CDI Extension}, that intercepts CDI injection targets.
 *
 * @author Jakub Podlesak
 */
public class CdiComponentProvider implements ComponentProvider, Extension {

    private static final Logger LOGGER = Logger.getLogger(CdiComponentProvider.class.getName());

    /**
     * Name to be used when binding CDI injectee skipping class analyzer to HK2 service injection manager.
     */
    public static final String CDI_CLASS_ANALYZER = "CdiInjecteeSkippingClassAnalyzer";

    private static final CdiComponentProviderRuntimeSpecifics runtimeSpecifics =
            CdiUtil.IS_SERVER_AVAILABLE
            ? new CdiComponentProviderServerRuntimeSpecifics()
            : new CdiComponentProviderClientRuntimeSpecifics();

    /**
     * set of non JAX-RS components containing JAX-RS injection points
     */
    private final Set<Type> jaxrsInjectableTypes = new HashSet<>();
    private final Set<Type> hk2ProvidedTypes = Collections.synchronizedSet(new HashSet<Type>());
    private final Set<Type> jerseyVetoedTypes = Collections.synchronizedSet(new HashSet<Type>());
    private final Set<DependencyPredicate> jerseyOrDependencyTypes = Collections.synchronizedSet(new LinkedHashSet<>());
    private final ThreadLocal<InjectionManager> threadInjectionManagers = new ThreadLocal<>();

    /**
     * set of request scoped components
     */
    private final Set<Class<?>> requestScopedComponents = new HashSet<>();


    private final Cache<Class<?>, Boolean> jaxRsComponentCache = new Cache<>(new Function<Class<?>, Boolean>() {
        @Override
        public Boolean apply(final Class<?> clazz) {
            return Application.class.isAssignableFrom(clazz)
                    || Providers.isJaxRsProvider(clazz)
                    || runtimeSpecifics.isJaxRsResource(clazz);
        }
    });

    private final Hk2CustomBoundTypesProvider customHk2TypesProvider;
    private final InjectionManagerStore injectionManagerStore;

    private volatile InjectionManager injectionManager;
    protected volatile javax.enterprise.inject.spi.BeanManager beanManager;

    private volatile Map<Class<?>, Set<Method>> methodsToSkip = new HashMap<>();
    private volatile Map<Class<?>, Set<Field>> fieldsToSkip = new HashMap<>();

    public CdiComponentProvider() {
        customHk2TypesProvider = CdiUtil.lookupService(Hk2CustomBoundTypesProvider.class);
        injectionManagerStore = CdiUtil.createHk2InjectionManagerStore();
        addHK2DepenendencyCheck(CdiComponentProvider::isJerseyOrDependencyType);
    }

    @Override
    public void initialize(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        this.beanManager = CdiUtil.getBeanManager();

        if (beanManager != null) {
            // Try to get CdiComponentProvider created by CDI.
            final CdiComponentProvider extension = beanManager.getExtension(CdiComponentProvider.class);

            if (extension != null) {
                extension.addInjectionManager(this.injectionManager);

                this.fieldsToSkip = extension.getFieldsToSkip();
                this.methodsToSkip = extension.getMethodsToSkip();

                bindHk2ClassAnalyzer();

                LOGGER.config(LocalizationMessages.CDI_PROVIDER_INITIALIZED());
            }
        }
    }

    @Override
    public boolean bind(final Class<?> clazz, final Set<Class<?>> providerContracts) {
        return bind(clazz, providerContracts, ContractProvider.NO_PRIORITY);
    }

    @Override
    public boolean bind(Class<?> component, ContractProvider contractProvider) {
        return contractProvider != null
                ? bind(component, contractProvider.getContracts(), contractProvider.getPriority(component))
                : bind(component, Collections.EMPTY_SET);
    }

    private boolean bind(final Class<?> clazz, final Set<Class<?>> providerContracts, Integer priority) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(LocalizationMessages.CDI_CLASS_BEING_CHECKED(clazz));
        }

        if (beanManager == null) {
            return false;
        }

        if (isJerseyOrDependencyType(clazz)) {
            return false;
        }

        final boolean isCdiManaged = isCdiComponent(clazz);
        final boolean isManagedBean = isManagedBean(clazz);
        final boolean isJaxRsComponent = isJaxRsComponentType(clazz);

        if (!isCdiManaged && !isManagedBean && !isJaxRsComponent) {
            return false;
        }

        final boolean isJaxRsResource = runtimeSpecifics.isJaxRsResource(clazz);

        if (isJaxRsResource && !runtimeSpecifics.isAcceptableResource(clazz)) {
            LOGGER.warning(LocalizationMessages.CDI_NON_INSTANTIABLE_COMPONENT(clazz));
            return false;
        }

        final Class<? extends Annotation> beanScopeAnnotation = CdiUtil.getBeanScope(clazz, beanManager);
        final boolean isRequestScoped = beanScopeAnnotation == RequestScoped.class
                || (beanScopeAnnotation == Dependent.class && isJaxRsResource);

        Supplier<AbstractCdiBeanSupplier> beanFactory = isRequestScoped
                ? new RequestScopedCdiBeanSupplier(clazz, injectionManager, beanManager, isCdiManaged)
                : new GenericCdiBeanSupplier(clazz, injectionManager, beanManager, isCdiManaged);

        SupplierInstanceBinding<AbstractCdiBeanSupplier> builder = Bindings.supplier(beanFactory)
                .to(clazz).qualifiedBy(CustomAnnotationLiteral.INSTANCE);
        for (Class<?> contract : providerContracts) {
            builder.to(contract);
        }
        if (priority > ContractProvider.NO_PRIORITY) {
            builder.ranked(priority);
        }

        injectionManager.register(builder);

        if (isRequestScoped) {
            requestScopedComponents.add(clazz);
        }

        if (LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config(LocalizationMessages.CDI_CLASS_BOUND_WITH_CDI(clazz));
        }

        return true;
    }

    @Override
    public void done() {
        if (requestScopedComponents.size() > 0) {
            InstanceBinding<ForeignRequestScopeBridge> descriptor = Bindings
                    .service((ForeignRequestScopeBridge) () -> requestScopedComponents)
                    .to(ForeignRequestScopeBridge.class);

            injectionManager.register(descriptor);

            if (LOGGER.isLoggable(Level.CONFIG)) {
                LOGGER.config(LocalizationMessages.CDI_REQUEST_SCOPED_COMPONENTS_RECOGNIZED(
                        listElements(new StringBuilder().append("\n"), requestScopedComponents).toString()));
            }
        }
    }

    private boolean isCdiComponent(final Class<?> component) {
        final Annotation[] qualifiers = CdiUtil.getQualifiers(component.getAnnotations());
        return !beanManager.getBeans(component, qualifiers).isEmpty();
    }

    private boolean isManagedBean(final Class<?> component) {
        return component.isAnnotationPresent(ManagedBean.class);
    }

    private AnnotatedConstructor<?> enrichedConstructor(final AnnotatedConstructor<?> ctor) {
        return new AnnotatedConstructor() {

            @Override
            public Constructor getJavaMember() {
                return ctor.getJavaMember();
            }

            @Override
            public List<AnnotatedParameter> getParameters() {
                final List<AnnotatedParameter> parameters = new ArrayList<>(ctor.getParameters().size());

                for (final AnnotatedParameter<?> ap : ctor.getParameters()) {
                    parameters.add(runtimeSpecifics.getAnnotatedParameter(ap));
                }
                return parameters;
            }

            @Override
            public boolean isStatic() {
                return ctor.isStatic();
            }

            @Override
            public AnnotatedType getDeclaringType() {
                return ctor.getDeclaringType();
            }

            @Override
            public Type getBaseType() {
                return ctor.getBaseType();
            }

            @Override
            public Set<Type> getTypeClosure() {
                return ctor.getTypeClosure();
            }

            @Override
            public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
                return ctor.getAnnotation(annotationType);
            }

            @Override
            public Set<Annotation> getAnnotations() {
                return ctor.getAnnotations();
            }

            @Override
            public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
                return ctor.isAnnotationPresent(annotationType);
            }
        };
    }

    private static boolean containsJaxRsConstructorInjection(final AnnotatedType annotatedType) {
        return containAnnotatedParameters(annotatedType.getConstructors(), runtimeSpecifics.getJaxRsInjectAnnotations());
    }

    private static boolean containsJaxRsMethodInjection(final AnnotatedType annotatedType) {
        return containAnnotatedParameters(annotatedType.getMethods(), runtimeSpecifics.getJaxRsInjectAnnotations());
    }

    private static boolean containsJaxRsFieldInjection(final AnnotatedType annotatedType) {
        return containAnnotation(annotatedType.getFields(), runtimeSpecifics.getJaxRsInjectAnnotations());
    }

    static boolean containAnnotatedParameters(final Collection<AnnotatedCallable> annotatedCallables,
                                                 final Set<Class<? extends Annotation>> annotationSet) {
        for (final AnnotatedCallable c : annotatedCallables) {
            if (containAnnotation(c.getParameters(), annotationSet)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containAnnotation(final Collection<Annotated> elements,
                                      final Set<Class<? extends Annotation>> annotationSet) {
        for (final Annotated element : elements) {
            if (hasAnnotation(element, annotationSet)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasAnnotation(final Annotated element, final Set<Class<? extends Annotation>> annotations) {
        for (final Class<? extends Annotation> a : annotations) {
            if (element.isAnnotationPresent(a)) {
                return true;
            }
        }
        return false;
    }

    public void processAnnotatedType(//@Observes
                                     // We can not apply the following constraint
                                     // if we want to fully support {@link org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider}.
                                     // Covered by tests/integration/cdi-with-jersey-injection-custom-cfg-webapp test application:
//                                      @WithAnnotations({
//                                              Context.class,
//                                              ApplicationPath.class,
//                                              HeaderParam.class,
//                                              QueryParam.class,
//                                              FormParam.class,
//                                              MatrixParam.class,
//                                              BeanParam.class,
//                                              PathParam.class})
                                     final ProcessAnnotatedType processAnnotatedType) {
        final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();

        // if one of the JAX-RS annotations is present in the currently seen class, add it to the "whitelist"
        if (containsJaxRsConstructorInjection(annotatedType)
                || containsJaxRsFieldInjection(annotatedType)
                || containsJaxRsMethodInjection(annotatedType)) {
            jaxrsInjectableTypes.add(annotatedType.getBaseType());
        }

        if (customHk2TypesProvider != null) {
            final Type baseType = annotatedType.getBaseType();
            if (customHk2TypesProvider.getHk2Types().contains(baseType)) {
                processAnnotatedType.veto();
                jerseyVetoedTypes.add(baseType);
            }
        }

        if (runtimeSpecifics.containsJaxRsParameterizedCtor(annotatedType)) {
            processAnnotatedType.setAnnotatedType(new AnnotatedType() {

                @Override
                public Class getJavaClass() {
                    return annotatedType.getJavaClass();
                }

                @Override
                public Set<AnnotatedConstructor> getConstructors() {
                    final Set<AnnotatedConstructor> result = new HashSet<>();
                    for (final AnnotatedConstructor c : annotatedType.getConstructors()) {
                        result.add(enrichedConstructor(c));
                    }
                    return result;
                }

                @Override
                public Set getMethods() {
                    return annotatedType.getMethods();
                }

                @Override
                public Set getFields() {
                    return annotatedType.getFields();
                }

                @Override
                public Type getBaseType() {
                    return annotatedType.getBaseType();
                }

                @Override
                public Set<Type> getTypeClosure() {
                    return annotatedType.getTypeClosure();
                }

                @Override
                public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
                    return annotatedType.getAnnotation(annotationType);
                }

                @Override
                public Set<Annotation> getAnnotations() {
                    return annotatedType.getAnnotations();
                }

                @Override
                public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
                    return annotatedType.isAnnotationPresent(annotationType);
                }
            });
        }
    }

    private Set<InjectionPoint> filterHk2InjectionPointsOut(final Set<InjectionPoint> originalInjectionPoints) {
        final Set<InjectionPoint> filteredInjectionPoints = new HashSet<>();
        for (final InjectionPoint ip : originalInjectionPoints) {
            final Type injectedType = ip.getType();
            if (customHk2TypesProvider != null && customHk2TypesProvider.getHk2Types().contains(injectedType)) {
                //remember the type, we would need to mock it's CDI binding at runtime
                hk2ProvidedTypes.add(injectedType);
            } else {
                if (injectedType instanceof Class<?>) {
                    final Class<?> injectedClass = (Class<?>) injectedType;
                    if (testDependencyType(injectedClass)) {
                        //remember the type, we would need to mock it's CDI binding at runtime
                        hk2ProvidedTypes.add(injectedType);
                    } else {
                        filteredInjectionPoints.add(ip);
                    }
                } else { // it is not a class, maybe provider type?:
                    if (isInjectionProvider(injectedType)
                            && (isProviderOfJerseyType((ParameterizedType) injectedType))) {
                        //remember the type, we would need to mock it's CDI binding at runtime
                        hk2ProvidedTypes.add(((ParameterizedType) injectedType).getActualTypeArguments()[0]);
                    } else {
                        filteredInjectionPoints.add(ip);
                    }
                }
            }
        }
        return filteredInjectionPoints;
    }

    private boolean isInjectionProvider(final Type injectedType) {
        return injectedType instanceof ParameterizedType
                && ((ParameterizedType) injectedType).getRawType() == javax.inject.Provider.class;
    }

    private boolean isProviderOfJerseyType(final ParameterizedType provider) {
        final Type firstArgumentType = provider.getActualTypeArguments()[0];
        if (firstArgumentType instanceof Class && isJerseyOrDependencyType((Class<?>) firstArgumentType)) {
            return true;
        }
        return (customHk2TypesProvider != null && customHk2TypesProvider.getHk2Types().contains(firstArgumentType));
    }

    private <T> void addInjecteeToSkip(final Class<?> componentClass, final Map<Class<?>, Set<T>> toSkip, final T member) {
        if (!toSkip.containsKey(componentClass)) {
            toSkip.put(componentClass, new HashSet<T>());
        }
        toSkip.get(componentClass).add(member);
    }

    /**
     * Auxiliary annotation for mocked beans used to cover Jersey/HK2 injected injection points.
     */
    @SuppressWarnings("serial")
    public static class CdiDefaultAnnotation extends AnnotationLiteral<Default> implements Default {

        private static final long serialVersionUID = 1L;
    }

    /**
     * Get the types provided by HK2
     * @return Types that HK2 is to inject
     */
    /* package */ boolean isHk2ProvidedType(Type type) {
        return hk2ProvidedTypes.contains(type);
    }

    /**
     * Gets you fields to skip from a proxied instance.
     * <p/>
     * Note: Do NOT lower the visibility of this method. CDI proxies need at least this visibility.
     *
     * @return fields to skip when injecting via HK2
     */
    /* package */ Map<Class<?>, Set<Field>> getFieldsToSkip() {
        return fieldsToSkip;
    }

    /**
     * Gets you methods to skip (from a proxied instance).
     * <p/>
     * Note: Do NOT lower the visibility of this method. CDI proxies need at least this visibility.
     *
     * @return methods to skip when injecting via HK2
     */
    /* package */ Map<Class<?>, Set<Method>> getMethodsToSkip() {
        return methodsToSkip;
    }

    /**
     * Gets you effective injection manager.
     * <p/>
     * Note: Do NOT lower the visibility of this method. CDI proxies need at least this visibility.
     *
     * @return HK2 injection manager.
     */
    /* package */ InjectionManager getEffectiveInjectionManager() {
        return injectionManagerStore.getEffectiveInjectionManager();
    }

    /**
     * Add HK2 {@link InjectionManager injection manager} (to a proxied instance).
     * <p/>
     * Note: Do NOT lower the visibility of this method. CDI proxies need at least this visibility.
     *
     * @param injectionManager injection manager.
     */
    /* package */ void addInjectionManager(final InjectionManager injectionManager) {
        injectionManagerStore.registerInjectionManager(injectionManager);
    }

    /**
     * Notifies the {@code InjectionTargetListener injection target listener} about new
     * {@link InjectionManagerInjectedTarget injected target}.
     * <p/>
     * Note: Do NOT lower the visibility of this method. CDI proxies need at least this visibility.
     *
     * @param target new injected target.
     */
    /* package */ void notify(final InjectionManagerInjectedTarget target) {
        if (injectionManagerStore instanceof InjectionTargetListener) {
            ((InjectionTargetListener) injectionManagerStore).notify(target);
        }
    }

    /**
     * Introspect given type to determine if it represents a JAX-RS component.
     *
     * @param clazz type to be introspected.
     * @return true if the type represents a JAX-RS component type.
     */
    /* package */ boolean isJaxRsComponentType(final Class<?> clazz) {
        return jaxRsComponentCache.apply(clazz);
    }

    private static boolean isJerseyOrDependencyType(final Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.isSynthetic()) {
            return false;
        }

        final Package pkg = clazz.getPackage();
        if (pkg == null) { // Class.getPackage() could return null
            LOGGER.warning(String.format("Class %s has null package", clazz));
            return false;
        }

        final String pkgName = pkg.getName();
        return !clazz.isAnnotationPresent(JerseyVetoed.class)
                && (pkgName.contains("org.glassfish.hk2")
                || pkgName.contains("jersey.repackaged")
                || pkgName.contains("org.jvnet.hk2")
                || (pkgName.startsWith("org.glassfish.jersey")
                && !pkgName.startsWith("org.glassfish.jersey.examples")
                && !pkgName.startsWith("org.glassfish.jersey.tests"))
                || (pkgName.startsWith("com.sun.jersey")
                && !pkgName.startsWith("com.sun.jersey.examples")
                && !pkgName.startsWith("com.sun.jersey.tests")));
    }

    private boolean testDependencyType(Class<?> clazz) {
        for (Predicate<Class<?>> predicate : jerseyOrDependencyTypes) {
            if (predicate.test(clazz)) {
                return true;
            }
        }
        return false;
    }

    private void bindHk2ClassAnalyzer() {
        ClassAnalyzer defaultClassAnalyzer =
                injectionManager.getInstance(ClassAnalyzer.class, ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME);

        int skippedElements = methodsToSkip.size() + fieldsToSkip.size();

        ClassAnalyzer customizedClassAnalyzer = skippedElements > 0
                ? new InjecteeSkippingAnalyzer(defaultClassAnalyzer, methodsToSkip, fieldsToSkip, beanManager)
                : defaultClassAnalyzer;

        Binder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bind(customizedClassAnalyzer)
                        .analyzeWith(ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME)
                        .to(ClassAnalyzer.class)
                        .named(CDI_CLASS_ANALYZER);
            }
        };
        injectionManager.register(binder);
    }

    private StringBuilder listElements(final StringBuilder logMsgBuilder, final Collection<? extends Object> elements) {
        for (final Object t : elements) {
            logMsgBuilder.append(String.format(" - %s%n", t));
        }
        return logMsgBuilder;
    }

    @SuppressWarnings("unchecked")
    /* package */ abstract class InjectionManagerInjectedCdiTarget implements InjectionManagerInjectedTarget {

        private final InjectionTarget delegate;
        private volatile InjectionManager effectiveInjectionManager;

        public InjectionManagerInjectedCdiTarget(InjectionTarget delegate) {
            this.delegate = delegate;
        }

        @Override
        public abstract Set<InjectionPoint> getInjectionPoints();

        @Override
        public void inject(final Object t, final CreationalContext cc) {
            InjectionManager injectingManager = getEffectiveInjectionManager();
            if (injectingManager == null) {
                injectingManager = effectiveInjectionManager;
                threadInjectionManagers.set(injectingManager);
            }

            delegate.inject(t, cc); // here the injection manager is used in HK2Bean

            if (injectingManager != null) {
                injectingManager.inject(t, CdiComponentProvider.CDI_CLASS_ANALYZER);
            }

            threadInjectionManagers.remove();
        }

        @Override
        public void postConstruct(final Object t) {
            delegate.postConstruct(t);
        }

        @Override
        public void preDestroy(final Object t) {
            delegate.preDestroy(t);
        }

        @Override
        public Object produce(final CreationalContext cc) {
            return delegate.produce(cc);
        }

        @Override
        public void dispose(final Object t) {
            delegate.dispose(t);
        }

        @Override
        public void setInjectionManager(final InjectionManager injectionManager) {
            this.effectiveInjectionManager = injectionManager;
        }
    }

    private class Hk2Bean implements Bean {

        private final Type t;

        public Hk2Bean(final Type t) {
            this.t = t;
        }

        @Override
        public Class getBeanClass() {
            return (Class) t;
        }

        @Override
        public Set getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public boolean isNullable() {
            return true;
        }

        @Override
        public Object create(final CreationalContext creationalContext) {
            InjectionManager injectionManager = getEffectiveInjectionManager();
            if (injectionManager == null) {
                injectionManager = threadInjectionManagers.get();
            }

            return injectionManager.getInstance(t);
        }

        @Override
        public void destroy(final Object instance, final CreationalContext creationalContext) {
        }

        @Override
        public Set getTypes() {
            return Collections.singleton(t);
        }

        @Override
        public Set getQualifiers() {
            return Collections.singleton(new CdiDefaultAnnotation());
        }

        @Override
        public Class getScope() {
            return Dependent.class;
        }

        @Override
        public String getName() {
            return t.toString();
        }

        @Override
        public Set getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }
    }

    // ------------------------------ CDI EXTENSIONS ------------------------------
    @SuppressWarnings("unused")
    private void processInjectionTarget(@Observes final ProcessInjectionTarget event) {
        final InjectionTarget it = event.getInjectionTarget();
        final Class<?> componentClass = event.getAnnotatedType().getJavaClass();

        final Set<InjectionPoint> cdiInjectionPoints = filterHk2InjectionPointsOut(it.getInjectionPoints());

        for (final InjectionPoint injectionPoint : cdiInjectionPoints) {
            final Member member = injectionPoint.getMember();
            if (member instanceof Field) {
                addInjecteeToSkip(componentClass, fieldsToSkip, (Field) member);
            } else if (member instanceof Method) {
                addInjecteeToSkip(componentClass, methodsToSkip, (Method) member);
            }
        }

        InjectionManagerInjectedCdiTarget target = null;
        if (isJerseyOrDependencyType(componentClass)) {
            target = new InjectionManagerInjectedCdiTarget(it) {

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    // Tell CDI to ignore Jersey (or it's dependencies) classes when injecting.
                    // CDI will not treat these classes as CDI beans (as they are not).
                    return Collections.emptySet();
                }
            };
        } else if (isJaxRsComponentType(componentClass)
                || jaxrsInjectableTypes.contains(event.getAnnotatedType().getBaseType())) {
            target = new InjectionManagerInjectedCdiTarget(it) {

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    // Inject CDI beans into JAX-RS resources/providers/application.
                    return cdiInjectionPoints;
                }
            };
        }

        if (target != null) {
            notify(target);
            //noinspection unchecked
            event.setInjectionTarget(target);
        }
    }


    @SuppressWarnings("unused")
    private void afterTypeDiscovery(@Observes final AfterTypeDiscovery afterTypeDiscovery) {
        if (LOGGER.isLoggable(Level.CONFIG) && !jerseyVetoedTypes.isEmpty()) {
            LOGGER.config(LocalizationMessages.CDI_TYPE_VETOED(customHk2TypesProvider,
                    listElements(new StringBuilder().append("\n"), jerseyVetoedTypes).toString()));
        }
    }

    @SuppressWarnings({"unused", "unchecked", "rawtypes"})
    private void afterDiscoveryObserver(@Observes final AfterBeanDiscovery abd) {
        if (customHk2TypesProvider != null) {
            hk2ProvidedTypes.addAll(customHk2TypesProvider.getHk2Types());
        }

        for (final Type t : hk2ProvidedTypes) {
            abd.addBean(new Hk2Bean(t));
        }
    }

    @SuppressWarnings("unused")
    private void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeBeanDiscovery, final BeanManager beanManager) {
        if (CdiUtil.IS_SERVER_AVAILABLE) {
            beforeBeanDiscovery.addAnnotatedType(
                    beanManager.createAnnotatedType(CdiComponentProviderServerRuntimeSpecifics.JaxRsParamProducer.class),
                    "Jersey " + CdiComponentProviderServerRuntimeSpecifics.JaxRsParamProducer.class.getName()
            );
        }

        beforeBeanDiscovery.addAnnotatedType(
                beanManager.createAnnotatedType(ProcessJAXRSAnnotatedTypes.class),
                "Jersey " + ProcessJAXRSAnnotatedTypes.class.getName()
        );
    }

    /**
     * Add a predicate to test HK2 dependency to create a CDI bridge bean to HK2 for it.
     * @param predicate to test whether given class is a HK2 dependency.
     */
    public void addHK2DepenendencyCheck(Predicate<Class<?>> predicate) {
        jerseyOrDependencyTypes.add(new DependencyPredicate(predicate));
    }

    private final class DependencyPredicate implements Predicate<Class<?>> {
        private final Predicate<Class<?>> predicate;

        public DependencyPredicate(Predicate<Class<?>> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean test(Class<?> aClass) {
            return predicate.test(aClass);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DependencyPredicate that = (DependencyPredicate) o;
            return predicate.getClass().equals(that.predicate);
        }

        @Override
        public int hashCode() {
            return predicate.getClass().hashCode();
        }
    }
}

