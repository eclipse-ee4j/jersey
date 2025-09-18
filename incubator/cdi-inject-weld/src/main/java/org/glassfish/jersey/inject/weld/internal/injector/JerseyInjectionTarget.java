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

package org.glassfish.jersey.inject.weld.internal.injector;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.ws.rs.WebApplicationException;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.InjectionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Interceptor;

import org.glassfish.jersey.inject.weld.internal.bean.BeanHelper;
import org.glassfish.jersey.inject.weld.internal.bean.JerseyBean;
import org.glassfish.jersey.inject.weld.internal.l10n.LocalizationMessages;
import org.glassfish.jersey.inject.weld.managed.CdiInjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.ConstructorInterceptionInstantiator;
import org.jboss.weld.injection.producer.DefaultInstantiator;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.injection.producer.InterceptorApplyingInstantiator;
import org.jboss.weld.injection.producer.SubclassDecoratorApplyingInstantiator;
import org.jboss.weld.injection.producer.SubclassedComponentInstantiator;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Formats;

/**
 * Wrapper for {@link InjectionTarget} that implements the functionality of injecting using JAX-RS annotations into provided
 * instances. {@code Delegate} is a original {@code InjectionTarget} which is able to inject other fields/parameters which
 * are managed by CDI.
 * <p>
 * Implementation is also able to create with custom {@code jerseyConstructor} if it is provided. This functionality allows override
 * default instantiator and use the Jersey-specific one.
 */
public class JerseyInjectionTarget<T> extends BasicInjectionTarget<T> {

    private static final Logger LOGGER = Logger.getLogger(JerseyInjectionTarget.class.getName());

    private final Bean<T> bean;
    private final Class<T> clazz;
    private final LazyValue<JerseyInstanceInjector<T>> injector;
    private final EnhancedAnnotatedType<T> enhancedAnnotatedType;
    private Collection<InjectionResolver> resolvers;
    private BasicInjectionTarget delegate; // for managed beans the initializeAfterBeanDiscovery is called for it
    private final Instantiator<T> instantiator;


    /**
     * Creates a new injection target which is able to delegate an injection to {@code delegate injection target} and inject
     * the fields that are Jersey-specific. The resolvers must be set later on. CDI will select its own constructor.
     *
     * @param delegate CDI specific injection target.
     * @param clazz    class that will be scanned and injected.
     */
    public JerseyInjectionTarget(BasicInjectionTarget<T> delegate, Class<T> clazz) {
        this(delegate, delegate.getBean(), clazz, null);
    }

    /**
     * Creates a new injection target which is able to delegate an injection to {@code delegate injection target} and inject
     * the fields that are Jersey-specific. CDI will select its own constructor.
     *
     * @param delegate  CDI specific injection target.
     * @param bean      bean which this injection target belongs to.
     * @param clazz     class that will be scanned and injected.
     * @param resolvers all resolvers that can provide a valued for Jersey-specific injection.
     */
    public JerseyInjectionTarget(BasicInjectionTarget<T> delegate, Bean<T> bean, Class<T> clazz,
            Collection<InjectionResolver> resolvers) {
        this(BeanHelper.createEnhancedAnnotatedType(delegate), delegate, bean, clazz, resolvers, delegate.getInstantiator());
        this.delegate = delegate;
        setInstantiator(this.instantiator);
    }

    /**
     * Creates a new injection target which is able to delegate an injection to {@code delegate injection target} and inject
     * the fields that are Jersey-specific. This method accepts custom instantiator, if the instantiator is {@code null}
     * default one is created.
     *
     * @param annotatedType resolved type of the registered bean.
     * @param delegate      CDI specific injection target.
     * @param bean          bean which this injection target belongs to.
     * @param clazz         class that will be scanned and injected.
     * @param resolvers     all resolvers that can provide a valued for Jersey-specific injection.
     * @param instantiator  default instantiator.
     */
    public JerseyInjectionTarget(EnhancedAnnotatedType<T> annotatedType, BasicInjectionTarget<T> delegate, Bean<T> bean,
            Class<T> clazz, Collection<InjectionResolver> resolvers, Instantiator<T> instantiator) {
        super(annotatedType,
                bean,
                delegate.getBeanManager(),
                delegate.getInjector(),
                delegate.getLifecycleCallbackInvoker(),
                JerseyBean.class.isInstance(bean)
                        ? new JerseyTwofoldInstantiator<T>((AbstractInstantiator<T>) instantiator)
                        : instantiator);

        this.bean = bean;
        this.enhancedAnnotatedType = annotatedType;
        this.clazz = clazz;
        this.resolvers = resolvers;
        this.injector = Values.lazy((Value<JerseyInstanceInjector<T>>) () -> new JerseyInstanceInjector<>(bean, this.resolvers));
        this.delegate = null;
        this.instantiator = getInstantiator();
        setInstantiator(this.instantiator);
    }

    @Override
    protected void checkDelegateInjectionPoints() {
        if (getAnnotatedType().getAnnotation(jakarta.decorator.Decorator.class) == null) {
            super.checkDelegateInjectionPoints();
        }
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        /*
         * If an instance contains any fields which be injected by Jersey then Jersey attempts to inject them using annotations
         * retrieves from registered InjectionResolvers.
         */
        try {
            injector.get().inject(instance);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Throwable cause) {
            throw injectionException(LocalizationMessages.IT_PROCESSING_ANNOTATION_EXCEPTION(clazz.getName()), cause);
        }

        /*
         * The rest of the fields (annotated by @Inject) are injected using CDI.
         */
        super.inject(instance, ctx);
    }

    /**
     * Copied method from the parent class because of a custom type of {@link Instantiator} is used in this implementation.
     *
     * @param annotatedType processed class.
     */
    @Override
    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        initializeInterceptionModel(annotatedType);

        InterceptionModel interceptionModel = null;
        if (isInterceptionCandidate()) {
            interceptionModel = beanManager.getInterceptorModelRegistry().get(getType());
        }
        boolean hasNonConstructorInterceptors = interceptionModel != null
                && (interceptionModel.hasExternalNonConstructorInterceptors()
                            || interceptionModel.hasTargetClassInterceptors());

        List<Decorator<?>> decorators = null;
        if (getBean() != null && isInterceptionCandidate()) {
            decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        }
        boolean hasDecorators = decorators != null && !decorators.isEmpty();
        if (hasDecorators) {
            checkDecoratedMethods(annotatedType, decorators);
        }

        if (hasNonConstructorInterceptors || hasDecorators) {
            if (!(getInstantiator() instanceof DefaultInstantiator<?>)) {
                throw illegalStateException(LocalizationMessages.IT_UNEXPECTED_INSTANTIATOR(getInstantiator()));
            }

            /*
             * Casting changed from DefaultInstantiator to a more abstract one because of using our custom JerseyInstantiator.
             */
            AbstractInstantiator<T> delegate = (AbstractInstantiator<T>) getInstantiator();
            setInstantiator(
                    SubclassedComponentInstantiator.forInterceptedDecoratedBean(annotatedType, getBean(), delegate, beanManager));

            if (hasDecorators) {
                setInstantiator(new SubclassDecoratorApplyingInstantiator<>(
                        getBeanManager().getContextId(), getInstantiator(), getBean(), decorators));
            }

            if (hasNonConstructorInterceptors) {
                setInstantiator(new InterceptorApplyingInstantiator<>(
                        getInstantiator(), interceptionModel, getType()));
            }
        }

        if (isInterceptionCandidate()) {
            setupConstructorInterceptionInstantiator(interceptionModel);
        }
    }

    private void setupConstructorInterceptionInstantiator(InterceptionModel interceptionModel) {
        if (interceptionModel != null && interceptionModel.hasExternalConstructorInterceptors()) {
            setInstantiator(new ConstructorInterceptionInstantiator<>(getInstantiator(), interceptionModel, getType()));
        }
    }

    private void checkNoArgsConstructor(EnhancedAnnotatedType<T> type) {
        if (!beanManager.getServices().get(ProxyInstantiator.class).isUsingConstructor()) {
            return;
        }
        EnhancedAnnotatedConstructor<T> constructor = type.getNoArgsEnhancedConstructor();
        if (constructor == null) {
            throw deploymentException(LocalizationMessages.IT_DECORATED_HAS_NO_NOARGS_CONSTRUCTOR(type));
        } else if (constructor.isPrivate()) {
            String stackTraceElement = Formats.formatAsStackTraceElement(constructor.getJavaMember());
            throw deploymentException(LocalizationMessages.IT_DECORATED_NOARGS_CONSTRUCTOR_PRIVATE(type, stackTraceElement));
        }
    }

    private void checkDecoratedMethods(EnhancedAnnotatedType<T> type, List<Decorator<?>> decorators) {
        if (type.isFinal()) {
            throw deploymentException(LocalizationMessages.IT_FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED(type));
        }
        checkNoArgsConstructor(type);
        for (Decorator<?> decorator : decorators) {
            EnhancedAnnotatedType<?> decoratorClass;
            if (decorator instanceof DecoratorImpl<?>) {
                DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
                decoratorClass = decoratorBean.getBeanManager().getServices().get(ClassTransformer.class)
                        .getEnhancedAnnotatedType(decoratorBean.getAnnotated());
            } else if (decorator instanceof CustomDecoratorWrapper<?>) {
                decoratorClass = ((CustomDecoratorWrapper<?>) decorator).getEnhancedAnnotated();
            } else {
                throw illegalStateException(LocalizationMessages.IT_NON_CONTAINER_DECORATOR(decorator));
            }

            for (EnhancedAnnotatedMethod<?, ?> decoratorMethod : decoratorClass.getEnhancedMethods()) {
                EnhancedAnnotatedMethod<?, ?> method = type.getEnhancedMethod(decoratorMethod.getSignature());
                if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal()) {
                    throw deploymentException(LocalizationMessages.IT_FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED(type));
                }
            }
        }
    }

    private void initializeInterceptionModel(EnhancedAnnotatedType<T> annotatedType) {
        AbstractInstantiator<T> instantiator = (AbstractInstantiator<T>) getInstantiator();
        if (instantiator.getConstructorInjectionPoint() == null) {
            return; // this is a non-producible InjectionTarget (only created to inject existing instances)
        }
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            buildInterceptionModel(annotatedType, instantiator);
        }
    }

    private void buildInterceptionModel(EnhancedAnnotatedType<T> annotatedType, AbstractInstantiator<T> instantiator) {
        new InterceptionModelInitializer<>(beanManager, annotatedType, annotatedType.getDeclaredEnhancedConstructor(
                instantiator.getConstructorInjectionPoint().getSignature()), getBean()).init();
    }

    private boolean isInterceptor() {
        return (getBean() instanceof Interceptor<?>) || getType().isAnnotationPresent(jakarta.interceptor.Interceptor.class);
    }

    private boolean isDecorator() {
        return (getBean() instanceof Decorator<?>) || getType().isAnnotationPresent(jakarta.decorator.Decorator.class);
    }

    private boolean isInterceptionCandidate() {
        return !isInterceptor() && !isDecorator() && !Modifier.isAbstract(getType().getJavaClass().getModifiers());
    }

    private static IllegalStateException illegalStateException(String message) {
        LOGGER.warning(message);
        return new IllegalStateException(message);
    }

    private static DeploymentException deploymentException(String message) {
        LOGGER.warning(message);
        return new DeploymentException(message);
    }

    private static InjectionException injectionException(String message, Throwable cause) {
        LOGGER.warning(message);
        return new InjectionException(message, cause);
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        T instance;
        if (delegate != null) {
            instance = (T) delegate.produce(ctx);
        } else {
            instance = super.produce(ctx);
            if (bean != null && !bean.getScope().equals(Dependent.class) && !getInstantiator().hasDecoratorSupport()) {
                // This should be safe, but needs verification PLM
                // Without this, the chaining of decorators will fail as the
                // incomplete instance will be resolved
                ctx.push(instance);
            }
        }
        if (CdiInjectionManagerFactory.SUPPORT_CONTEXT) {
            InjectionManager injectionManager = CdiInjectionManagerFactory.getInjectionManager(ctx);
            injectionManager.inject(instance, "CONTEXT");
        }
        return instance;
    }

    public Instantiator<T> getInstantiator() {
        return delegate != null ? delegate.getInstantiator() : super.getInstantiator();
    }

    public void setInstantiator(Instantiator<T> instantiator) {
        if (this.delegate != null) {
            delegate.setInstantiator(instantiator);
        } else {
            super.setInstantiator(instantiator);
        }
    }

    @Override
    public Bean<T> getBean() {
        return this.bean;
    }

    /**
     * In some cases Injection Resolvers cannot be provided during th creation of the object therefore must be set later on.
     *
     * @param resolvers all registered injection resolvers.
     */
    public void setInjectionResolvers(Collection<InjectionResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public EnhancedAnnotatedType<T> getEnhancedAnnotatedType() {
        return enhancedAnnotatedType;
    }

    public JerseyTwofoldInstantiator<T> getTwofoldInstantiator() {
        return (JerseyTwofoldInstantiator<T>) instantiator;
    }
}
