/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.managed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Singleton;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.internal.inject.InitializableInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.inject.InitializableSupplierInstanceBinding;
import org.glassfish.jersey.inject.weld.internal.bean.JerseyBean;
import org.glassfish.jersey.inject.weld.internal.inject.MatchableBinding;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.inject.ServiceHolderImpl;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;

/**
 * Implementation of {@link InjectionManager} used on the server side.
 */
@Singleton
public class CdiInjectionManager implements InjectionManager {

    private final BeanManager beanManager;
    private final Binder bindings;
    private boolean isCompleted = false;
    Set<Class<?>> managedBeans;

    // Keeps all binders and bindings added to the InjectionManager during the bootstrap.

    public CdiInjectionManager(BeanManager beanManager, Binder bindings) {
        this.beanManager = beanManager;
        this.bindings = bindings;
    }

    @Override
    public void register(Binding binding) {
        if (isManagedClass(binding)) {
            return;
        }
        if (InstanceBinding.class.isInstance(binding)) {
            final Collection<Binding> preBindings = bindings.getBindings();
            MatchableBinding.Matching<InitializableInstanceBinding> matching = MatchableBinding.Matching.noneMatching();
            for (Binding preBinding : preBindings) {
                if (InitializableInstanceBinding.class.isInstance(preBinding)) {
                    matching = matching.better(((InitializableInstanceBinding) preBinding).matches((InstanceBinding) binding));
                    if (matching.isBest()) {
                        break;
                    }
                }
            }
            if (matching.matches()) {
               matching.getBinding().init(((InstanceBinding) binding).getService());
            } else if (findClassBinding(binding.getImplementationType()) == null) {
                throw new IllegalStateException("Not initialized " + ((InstanceBinding<?>) binding).getService());
            }
        } else if (SupplierInstanceBinding.class.isInstance(binding)) {
            final Collection<Binding> preBindings = bindings.getBindings();
            MatchableBinding.Matching<InitializableSupplierInstanceBinding> matching = MatchableBinding.Matching.noneMatching();
            for (Binding preBinding : preBindings) {
                if (InitializableSupplierInstanceBinding.class.isInstance(preBinding)) {
                    matching = matching.better(
                            ((InitializableSupplierInstanceBinding) preBinding).matches((SupplierInstanceBinding) binding));
                    if (matching.isBest()) {
                        break;
                    }
                }
            }
            if (matching.matches()) {
                matching.getBinding().init(((SupplierInstanceBinding) binding).getSupplier());
            } else {
                throw new IllegalStateException("Not initialized " + ((SupplierInstanceBinding<?>) binding).getSupplier());
            }
        } else if (ClassBinding.class.isInstance(binding)) {
            if (findClassBinding(binding.getImplementationType()) == null) {
//            final Collection<Binding> preBindings = bindings.getBindings();
//            boolean found = false;
//            for (Binding preBinding : preBindings) {
//                if (ClassBinding.class.isInstance(preBinding)
//                        && ((ClassBinding) preBinding).getImplementationType()
//                            .equals(((ClassBinding) binding).getImplementationType())) {
//                        found = true;
//                        break;
//                }
//            }
//            if (!found) {
                throw new IllegalStateException("ClassBinding for " + binding.getImplementationType() + " not preregistered");
            }
        }
    }

    private <T> ClassBinding<T> findClassBinding(Class<T> implementationType) {
        final Collection<Binding> preBindings = bindings.getBindings();
        boolean found = false;
        for (Binding preBinding : preBindings) {
            if (ClassBinding.class.isInstance(preBinding)
                    && ((ClassBinding) preBinding).getImplementationType().equals(implementationType)) {
                return (ClassBinding<T>) preBinding;
            }
        }
        return null;
    }

    private boolean isManagedClass(Binding binding) {
        return managedBeans != null
                && binding.getImplementationType() != null
                && (managedBeans.contains(binding.getImplementationType())
                    || (managedBeans.contains(binding.getImplementationType().getSuperclass())));
    }

    @Override
    public void register(Iterable<Binding> bindings) {
        for (Binding binding : bindings) {
            register(binding);
        }
    }

    @Override
    public void register(Binder binder) {
        for (Binding binding : Bindings.getBindings(this, binder)) {
            register(binding);
        }
    }

    @Override
    public void register(Object provider) throws IllegalArgumentException {
        throw new IllegalArgumentException(LocalizationMessages.CDI_2_PROVIDER_NOT_REGISTRABLE(provider.getClass()));
    }

    @Override
    public boolean isRegistrable(Class<?> clazz) {
        return false;
    }

    @Override
    public <T> T create(Class<T> createMe) {
        Unmanaged.UnmanagedInstance<T> unmanaged = new Unmanaged<>(createMe).newInstance();
        return unmanaged.produce().get();
    }

    @Override
    public <T> T createAndInitialize(Class<T> createMe) {
        Unmanaged.UnmanagedInstance<T> unmanaged = new Unmanaged<>(createMe).newInstance();
        return unmanaged.produce()
                .inject()
                .postConstruct()
                .get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
        List<ServiceHolder<T>> result = new ArrayList<>();
        for (Bean<?> bean : beanManager.getBeans(contractOrImpl, qualifiers)) {

            if (!isRuntimeTypeBean(bean)) {
                continue;
            }

            CreationalContext<?> ctx = createCreationalContext(bean);
            T reference = (T) beanManager.getReference(bean, contractOrImpl, ctx);

            int rank = 1;
            if (bean instanceof JerseyBean) {
                rank = ((JerseyBean) bean).getRank();
            }

            result.add(new ServiceHolderImpl<>(reference, (Class<T>) bean.getBeanClass(), bean.getTypes(), rank));
        }
        return result;
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
        return getInstanceInternal(contractOrImpl, qualifiers);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl) {
        return getInstanceInternal(contractOrImpl);
    }

    @Override
    public <T> T getInstance(Type contractOrImpl) {
        return getInstanceInternal(contractOrImpl);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getInstanceInternal(Type contractOrImpl, Annotation... qualifiers) {
//        if (contractOrImpl.getTypeName().contains("HelloResource")) {
//            T t = (T) CDI.current().select((Class) contractOrImpl, qualifiers).get();
//            try {
//                System.out.println(t.getClass().getMethod("hello").invoke(t));
////                t.getClass().getMethod("hello").invoke(t);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//            return t;
//        }
        Set<Bean<?>> beans = beanManager.getBeans(contractOrImpl, qualifiers);
        if (beans.isEmpty()) {
            return null; //ScopesTest
        }

        final Iterator<?> beansIterator = beans.iterator();
        Bean<?> bean = (Bean<?>) beansIterator.next();
        while (beansIterator.hasNext() && !JerseyBean.class.isInstance(bean) && !isRuntimeTypeBean(bean)) {
            bean = (Bean<?>) beansIterator.next(); // prefer Jersey binding
        }
        CreationalContext<T> ctx = createCreationalContext((Bean<T>) bean);
        return (T) beanManager.getReference(bean, contractOrImpl, ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getInstance(ForeignDescriptor foreignDescriptor) {
        Bean bean = (Bean) foreignDescriptor.get();
        CreationalContext ctx = createCreationalContext(bean);
        return bean.create(ctx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ForeignDescriptor createForeignDescriptor(Binding binding) {
        Class<?> clazz;
        if (ClassBinding.class.isAssignableFrom(binding.getClass())) {
            clazz = ((ClassBinding<?>) binding).getService();
        } else if (InstanceBinding.class.isAssignableFrom(binding.getClass())) {
            clazz = ((InstanceBinding<?>) binding).getService().getClass();
        } else {
            throw new RuntimeException(
                    org.glassfish.jersey.internal.LocalizationMessages
                            .UNKNOWN_DESCRIPTOR_TYPE(binding.getClass().getSimpleName()));
        }

        Set<Bean<?>> beans = beanManager.getBeans(clazz);
        if (beans.isEmpty()) {
            return null;
        }

        Bean bean = beans.iterator().next();
        CreationalContext ctx = createCreationalContext(bean);
        return ForeignDescriptor.wrap(bean, instance -> bean.destroy(instance, ctx));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllInstances(Type contractOrImpl) {
        List<T> result = new ArrayList<>();
        for (Bean<?> bean : beanManager.getBeans(contractOrImpl)) {
            CreationalContext<?> ctx = createCreationalContext(bean);
            Object reference = beanManager.getReference(bean, contractOrImpl, ctx);
            result.add((T) reference);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void inject(Object instance) {
        CreationalContext creationalContext = createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType((Class) instance.getClass());
        InjectionTargetFactory injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        InjectionTarget injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        injectionTarget.inject(instance, creationalContext);
    }

    @Override
    public void preDestroy(Object preDestroyMe) {
    }

    @Override
    public void completeRegistration() throws IllegalStateException {
//        bindings.bind(Bindings.service(this).to(InjectionManager.class));
//        bindings.install(new ContextInjectionResolverImpl.Binder(this::getBeanManager));
//
//        this.beanManager = new DefaultBeanManagerProvider().getBeanManager();
//        beanManager = new DefaultBeanManagerProvider().getBeanManager();
//
//        AbstractBinder masterBinder = beanManager.getExtension(SeBeanRegisterExtension.class).bindings;
//        masterBinder.getBindings().addAll(bindings.getBindings());

//        bindings.bind(Bindings.service(this).to(InjectionManager.class));
//        bindings.install(new ContextInjectionResolverImpl.Binder(this::getBeanManager));

        if (!isCompleted) {
            register(Bindings.service(this).to(InjectionManager.class));
            isCompleted = false;
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    protected Binder getBindings() {
        return bindings;
    }

    protected BeanManager getBeanManager() {
        return beanManager;
    }

    protected <T> CreationalContext<T> createCreationalContext(Bean<T> bean) {
        return (CreationalContext<T>) beanManager.createCreationalContext(bean);
    }

//    protected void registerInjectionResolver(InjectionResolverBinding<?> injectionResolverBinding) {
//       // beanManager.getExtension(BinderRegisterExtension.class).addInjectionResolver(injectionResolverBinding.getResolver());
//    }

    /**
     * Identifies Jersey beans that are from different runtime (CLIENT vs SERVER). Used to exclude Jersey beans of incorrect
     * {@link RuntimeType}.
     * @param bean the given CDI bean.
     * @return true iff the given bean is not a Jersey Bean or the Jersey Bean is of the proper {@code RuntimeType}.
     */
    protected boolean isRuntimeTypeBean(Bean<?> bean) {
        return !JerseyBean.class.isInstance(bean) || ((JerseyBean) bean).getRutimeType() == RuntimeType.SERVER;
    }

    @Override
    public void inject(Object injectMe, String classAnalyzer) {
        // TODO: Used only in legacy CDI integration.
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
        // TODO: Used only in legacy CDI integration.
        throw new UnsupportedOperationException();
    }
}
