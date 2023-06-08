/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.innate.inject;

import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.ClassBinding;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.inject.ServiceHolderImpl;
import org.glassfish.jersey.internal.inject.SupplierClassBinding;
import org.glassfish.jersey.internal.inject.SupplierInstanceBinding;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ConstrainedTo(RuntimeType.CLIENT)
public final class NonInjectionManager implements InjectionManager {
    private static final Logger logger = Logger.getLogger(NonInjectionManager.class.getName());

    private final MultivaluedMap<Class<?>, InstanceBinding<?>> instanceBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Class<?>, ClassBinding<?>> contractBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Class<?>, SupplierInstanceBinding<?>> supplierInstanceBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Class<?>, SupplierClassBinding<?>> supplierClassBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Type, InstanceBinding<?>> instanceTypeBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Type, ClassBinding<?>> contractTypeBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Type, SupplierInstanceBinding<?>> supplierTypeInstanceBindings = new MultivaluedHashMap<>();
    private final MultivaluedMap<Type, SupplierClassBinding<?>> supplierTypeClassBindings = new MultivaluedHashMap<>();

    private final MultivaluedMap<DisposableSupplier, Object> disposableSupplierObjects = new MultivaluedHashMap<>();

    private final Instances instances = new Instances();
    private final Types types = new Types();

    private volatile boolean isRequestScope = false;
    private volatile boolean shutdown = false;

    /**
     * A class that holds singleton instances and thread-scope instances. Provides thread safe access to singletons
     * and thread-scope instances. The instances are created for Type (ParametrizedType) and for a Class.
     * @param <TYPE> the type for which the instance is created, either Class, or ParametrizedType (for instance
     * Provider&lt;SomeClass&gt;).
     */
    private class TypedInstances<TYPE> {
        private final MultivaluedMap<TYPE, InstanceContext<?>> singletonInstances = new MultivaluedHashMap<>();
        private final ThreadLocal<MultivaluedMap<TYPE, InstanceContext<?>>> threadInstances = new ThreadLocal<>();
        private final List<Object> threadPredestroyables = Collections.synchronizedList(new LinkedList<>());

        private <T> List<InstanceContext<?>> _getSingletons(TYPE clazz) {
            List<InstanceContext<?>> si;
            synchronized (singletonInstances) {
                si = singletonInstances.get(clazz);
            }
            return si;
        }

        @SuppressWarnings("unchecked")
        <T> T _addSingleton(TYPE clazz, T instance, Binding<?, ?> binding, Annotation[] qualifiers) {
            synchronized (singletonInstances) {
                // check existing singleton with a qualifier already created by another thread io a meantime
                List<InstanceContext<?>> values = singletonInstances.get(clazz);
                if (values != null) {
                    List<InstanceContext<?>> qualified
                            = values.stream()
                                    .filter(ctx -> ctx.hasQualifiers(qualifiers))
                                    .collect(Collectors.toList());
                    if (!qualified.isEmpty()) {
                        return (T) qualified.get(0).instance;
                    }
                }
                singletonInstances.add(clazz, new InstanceContext<>(instance, binding, qualifiers));
                threadPredestroyables.add(instance);
                return instance;
            }
        }

        @SuppressWarnings("unchecked")
        <T> T addSingleton(TYPE clazz, T t, Binding<?, ?> binding, Annotation[] instanceQualifiers) {
            T t2  = _addSingleton(clazz, t, binding, instanceQualifiers);
            if (t2 == t) {
                for (Type contract : binding.getContracts()) {
                    if (!clazz.equals(contract) && isClass(contract)) {
                        _addSingleton((TYPE) contract, t, binding, instanceQualifiers);
                    }
                }
            }
            return t2;
        }

        private List<InstanceContext<?>> _getThreadInstances(TYPE clazz) {
            MultivaluedMap<TYPE, InstanceContext<?>> ti = threadInstances.get();
            List<InstanceContext<?>> list = ti == null ? null : new LinkedList<>();
            if (ti != null) {
                return ti.get(clazz);
            }
            return list;
        }

        private <T> void _addThreadInstance(TYPE clazz, T instance, Binding<T, ?> binding, Annotation[] qualifiers) {
            MultivaluedMap<TYPE, InstanceContext<?>> map = threadInstances.get();
            if (map == null) {
                map = new MultivaluedHashMap<>();
                threadInstances.set(map);
            }
            map.add(clazz, new InstanceContext<>(instance, binding, qualifiers));
            threadPredestroyables.add(instance);
        }

        <T> void addThreadInstance(TYPE clazz, T t, Binding<T, ?> binding, Annotation[] instanceQualifiers) {
            _addThreadInstance(clazz, t, binding, instanceQualifiers);
            for (Type contract : binding.getContracts()) {
                if (!clazz.equals(contract) && isClass(contract)) {
                    _addThreadInstance((TYPE) contract, t, binding, instanceQualifiers);
                }
            }
        }

        private <T> List<T> getInstances(TYPE clazz, Annotation[] annotations) {
            List<InstanceContext<?>> i = _getContexts(clazz);
            return InstanceContext.toInstances(i, annotations);
        }

        <T> List<InstanceContext<?>> getContexts(TYPE clazz, Annotation[] annotations) {
            List<InstanceContext<?>> i = _getContexts(clazz);
            return InstanceContext.filterInstances(i, annotations);
        }

        private <T> List<InstanceContext<?>> _getContexts(TYPE clazz) {
            List<InstanceContext<?>> si = _getSingletons(clazz);
            List<InstanceContext<?>> ti = _getThreadInstances(clazz);
            if (si == null && ti != null) {
                si = ti;
            } else if (ti != null) {
                si.addAll(ti);
            }
            return si;
        }

        <T> T getInstance(TYPE clazz, Annotation[] annotations) {
            List<T> i = getInstances(clazz, annotations);
            if (i != null) {
                checkUnique(i);
                return i.get(0);
            }
            return null;
        }

        void dispose() {
            singletonInstances.forEach((clazz, instances) -> instances.forEach(instance -> preDestroy(instance.getInstance())));
            threadPredestroyables.forEach(NonInjectionManager.this::preDestroy);
        }
    }

    private class Instances extends TypedInstances<Class<?>> {
    }

    private class Types extends TypedInstances<Type> {
    }

    public NonInjectionManager() {
    }

    public NonInjectionManager(boolean warning) {
        if (warning) {
            logger.warning(LocalizationMessages.NONINJECT_FALLBACK());
        } else {
            logger.log(Level.FINER, LocalizationMessages.NONINJECT_FALLBACK());
        }
    }

    @Override
    public void completeRegistration() {
        instances._addSingleton(InjectionManager.class, this, new InjectionManagerBinding(), null);
    }

    @Override
    public void shutdown() {
        shutdown = true;

        disposableSupplierObjects.forEach((supplier, objects) -> objects.forEach(supplier::dispose));
        disposableSupplierObjects.clear();

        instances.dispose();
        types.dispose();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    private void checkShutdown() {
        if (shutdown) {
            throw new IllegalStateException(LocalizationMessages.NONINJECT_SHUTDOWN());
        }
    }

    @Override
    public void register(Binding binding) {
        checkShutdown();
        if (InstanceBinding.class.isInstance(binding)) {
            InstanceBinding instanceBinding = (InstanceBinding) binding;
            Class<?> mainType = binding.getImplementationType();
            if (!instanceBindings.containsKey(mainType)) { // the class could be registered twice, for reader & for writer
                instanceBindings.add(mainType, (InstanceBinding) binding);
            }
            for (Type type : (Iterable<Type>) instanceBinding.getContracts()) {
                if (isClass(type)) {
                    if (!mainType.equals(type)) {
                        instanceBindings.add((Class<?>) type, instanceBinding);
                    }
                } else {
                    instanceTypeBindings.add(type, instanceBinding);
                }
            }
        } else if (ClassBinding.class.isInstance(binding)) {
            ClassBinding<?> contractBinding = (ClassBinding<?>) binding;
            Class<?> mainType = binding.getImplementationType();
            if (!contractBindings.containsKey(mainType)) { // the class could be registered twice, for reader & for writer
                contractBindings.add(mainType, contractBinding);
            }
            for (Type type : contractBinding.getContracts()) {
                if (isClass(type)) {
                    if (!mainType.equals(type)) {
                        contractBindings.add((Class<?>) type, contractBinding);
                    }
                } else {
                    contractTypeBindings.add(type, contractBinding);
                }
            }
        } else if (SupplierInstanceBinding.class.isInstance(binding)) {
            SupplierInstanceBinding<?> supplierBinding = (SupplierInstanceBinding<?>) binding;
            for (Type type : supplierBinding.getContracts()) {
                if (isClass(type)) {
                    supplierInstanceBindings.add((Class<?>) type, supplierBinding);
                } else {
                    supplierTypeInstanceBindings.add(type, supplierBinding);
                }
            }
        } else if (SupplierClassBinding.class.isInstance(binding)) {
            SupplierClassBinding<?> supplierBinding = (SupplierClassBinding<?>) binding;
            for (Type type : supplierBinding.getContracts()) {
                if (isClass(type)) {
                    supplierClassBindings.add((Class<?>) type, supplierBinding);
                } else {
                    supplierTypeClassBindings.add(type, supplierBinding);
                }
            }
        }
    }

    @Override
    public void register(Iterable<Binding> descriptors) {
        checkShutdown();
        for (Binding binding : descriptors) {
            register(binding);
        }
    }

    @Override
    public void register(Binder binder) {
        checkShutdown();
        binder.getBindings().stream().iterator().forEachRemaining(this::register);
    }

    @Override
    public void register(Object provider) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Register " + provider);
    }

    @Override
    public boolean isRegistrable(Class<?> clazz) {
        return false; // for external creators
    }

    @Override
    public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
        checkShutdown();

        ClassBindings<T> classBindings = classBindings(contractOrImpl, qualifiers);
        return classBindings.getAllServiceHolders(qualifiers);
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
        checkShutdown();

        ClassBindings<T> classBindings = classBindings(contractOrImpl, qualifiers);
        classBindings.matchQualifiers(qualifiers);
        return classBindings.getInstance();
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
        throw new UnsupportedOperationException("getInstance(Class, String)");
    }

    @Override
    public <T> T getInstance(Class<T> contractOrImpl) {
        checkShutdown();

        T instance = instances.getInstance(contractOrImpl, null);
        if (instance != null) {
            return instance;
        }
        return create(contractOrImpl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Type contractOrImpl) {
        checkShutdown();

        if (ParameterizedType.class.isInstance(contractOrImpl)) {
            T instance = types.getInstance(contractOrImpl, null);
            if (instance != null) {
                return instance;
            }

            TypeBindings<T> typeBindings = typeBindings(contractOrImpl);
            return typeBindings.getInstance();
        } else if (isClass(contractOrImpl)) {
            return getInstance((Class<? extends T>) contractOrImpl);
        }
        throw new IllegalStateException(LocalizationMessages.NONINJECT_UNSATISFIED(contractOrImpl));
    }

    private static boolean isClass(Type type) {
        return Class.class.isAssignableFrom(type.getClass());
    }

    @Override
    public Object getInstance(ForeignDescriptor foreignDescriptor) {
        throw new UnsupportedOperationException("getInstance(ForeignDescriptor foreignDescriptor) ");
    }

    @Override
    public ForeignDescriptor createForeignDescriptor(Binding binding) {
        throw new UnsupportedOperationException("createForeignDescriptor(Binding binding) ");
    }

    @Override
    public <T> List<T> getAllInstances(Type contractOrImpl) {
        checkShutdown();

        if (!isClass(contractOrImpl)) {
            TypeBindings<T> typeBindings = typeBindings(contractOrImpl);
            return typeBindings.allInstances();
        }

        @SuppressWarnings("unchecked")
        ClassBindings<T> classBindings = classBindings((Class<T>) contractOrImpl);
        return classBindings.allInstances();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> createMe) {
        checkShutdown();

        if (InjectionManager.class.equals(createMe)) {
            return (T) this;
        }
        if (RequestScope.class.equals(createMe)) {
            if (!isRequestScope) {
                isRequestScope = true;
                return (T) new NonInjectionRequestScope();
            } else {
                throw new IllegalStateException(LocalizationMessages.NONINJECT_REQUESTSCOPE_CREATED());
            }
        }

        ClassBindings<T> classBindings = classBindings(createMe);
        return classBindings.create(true);
    }

    @Override
    public <T> T createAndInitialize(Class<T> createMe) {
        checkShutdown();

        if (InjectionManager.class.equals(createMe)) {
            return (T) this;
        }
        if (RequestScope.class.equals(createMe)) {
            if (!isRequestScope) {
                isRequestScope = true;
                return (T) new NonInjectionRequestScope();
            } else {
                throw new IllegalStateException(LocalizationMessages.NONINJECT_REQUESTSCOPE_CREATED());
            }
        }

        ClassBindings<T> classBindings = classBindings(createMe);
        T t = classBindings.create(false);
        return t != null ? t : justCreate(createMe);
    }

    public <T> T justCreate(Class<T> createMe) {
        T result = null;
        try {
            Constructor<T> mostArgConstructor = findConstructor(createMe);
            if (mostArgConstructor != null) {
                int argCount = mostArgConstructor.getParameterCount();
                if (argCount == 0) {
                    ensureAccessible(mostArgConstructor);
                    result = mostArgConstructor.newInstance();
                } else if (argCount > 0) {
                    Object[] args = getArguments(mostArgConstructor, argCount);
                    if (args != null) {
                        ensureAccessible(mostArgConstructor);
                        result = mostArgConstructor.newInstance(args);
                    }
                }
            }
            if (result == null) {
                throw new IllegalStateException(LocalizationMessages.NONINJECT_NO_CONSTRUCTOR(createMe.getName()));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        inject(result);
        return result;
    }

    private static <T> Constructor<T> findConstructor(Class<T> forClass) {
        Constructor<T>[] constructors = (Constructor<T>[]) forClass.getDeclaredConstructors();
        Constructor<T> mostArgConstructor = null;
        int argCount = -1;
        for (Constructor<T> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class) || constructor.getParameterCount() == 0) {
                if (constructor.getParameterCount() > argCount) {
                    mostArgConstructor = constructor;
                    argCount = constructor.getParameterCount();
                }
            }
        }
        return mostArgConstructor;
    }

    private Object[] getArguments(Executable executable, int argCount) {
        if (executable == null) {
            return null;
        }
        Object[] args = new Object[argCount];
        for (int i = 0; i != argCount; i++) {
            Type type = executable.getAnnotatedParameterTypes()[i].getType();
            args[i] = isClass(type) ? getInstance((Class<?>) type) : getInstance(type);
        }
        return args;
    }

    private static void ensureAccessible(Executable executable) {
        try {
            if (!executable.isAccessible()) {
                executable.setAccessible(true);
            }
        } catch (Exception e) {
            // consume. It will fail later with invoking the executable
        }
    }

    private void checkUnique(List<?> list) {
        if (list.size() != 1) {
            throw new IllegalStateException(LocalizationMessages.NONINJECT_AMBIGUOUS_SERVICES(list.get(0)));
        }
    }

    @Override
    public void inject(Object injectMe) {
        Method postConstruct = getAnnotatedMethod(injectMe, PostConstruct.class);
        if (postConstruct != null) {
            ensureAccessible(postConstruct);
            try {
                postConstruct.invoke(injectMe);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void inject(Object injectMe, String classAnalyzer) {
        throw new UnsupportedOperationException("inject(Object injectMe, String classAnalyzer)");
    }

    @Override
    public void preDestroy(Object preDestroyMe) {
        Method preDestroy = getAnnotatedMethod(preDestroyMe, PreDestroy.class);
        if (preDestroy != null) {
            ensureAccessible(preDestroy);
            try {
                preDestroy.invoke(preDestroyMe);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static Method getAnnotatedMethod(Object object, Class<? extends Annotation> annotation) {
        Class<?> clazz = object.getClass();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotation)
                    && /* do not invoke interceptors */ method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }

    /**
     * Some {@link Binding} requires the proxy to be created rather than just the instance,
     * for instance a proxy of an instance supplied by a supplier that is not known at a time of the proxy creation.
     * @param createProxy the nullable {@link Binding#isProxiable()} information
     * @param iface the type of which the proxy is created
     * @param supplier the reference to the supplier
     * @param <T> the type the supplier should supply
     * @return The proxy for the instance supplied by a supplier or the instance if not required to be proxied.
     */
    @SuppressWarnings("unchecked")
    private <T> T createSupplierProxyIfNeeded(Boolean createProxy, Class<T> iface, Supplier<T> supplier) {
        if (createProxy != null && createProxy && iface.isInterface()) {
            T proxy = (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, new InvocationHandler() {
                final SingleRegisterSupplier<T> singleSupplierRegister = new SingleRegisterSupplier<>(supplier);
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    T t = singleSupplierRegister.get();
                    Object ret = method.invoke(t, args);
                    return ret;
                }
            });
            return proxy;
        } else {
            return registerDisposableSupplierAndGet(supplier);
        }
    }

    /**
     * A holder class making sure the Supplier (especially the {@link DisposableSupplier}) supplying the instance
     * supplies (and is registered for being disposed at the end of the lifecycle) only once.
     * @param <T>
     */
    private class SingleRegisterSupplier<T> {
        private final LazyValue<T> once;

        private SingleRegisterSupplier(Supplier<T> supplier) {
            once = Values.lazy((Value<T>) () -> registerDisposableSupplierAndGet(supplier));
        }

        T get() {
            return once.get();
        }
    }

    private <T> T registerDisposableSupplierAndGet(Supplier<T> supplier) {
        T instance = supplier.get();
        if (DisposableSupplier.class.isInstance(supplier)) {
            disposableSupplierObjects.add((DisposableSupplier<T>) supplier, instance);
        }
        return instance;
    }

    /**
     * Create {@link ClassBindings} instance containing bindings and instances for the given Type.
     * @param clazz the given class.
     * @param instancesQualifiers The qualifiers the expected instances of the given class should have.
     * @param <T> Expected return class type.
     * @return the {@link ClassBindings}.
     */
    @SuppressWarnings("unchecked")
    private <T> ClassBindings<T> classBindings(Class<T> clazz, Annotation... instancesQualifiers) {
        ClassBindings<T> classBindings = new ClassBindings<>(clazz, instancesQualifiers);
        List<InstanceBinding<?>> ib = instanceBindings.get(clazz);
        if (ib != null) {
            ib.forEach(binding -> classBindings.instanceBindings.add((InstanceBinding<T>) binding));
        }
        List<SupplierInstanceBinding<?>> sib = supplierInstanceBindings.get(clazz);
        if (sib != null) {
            sib.forEach(binding -> classBindings.supplierInstanceBindings.add((SupplierInstanceBinding<T>) binding));
        }
        List<ClassBinding<?>> cb = contractBindings.get(clazz);
        if (cb != null) {
            cb.forEach(binding -> classBindings.classBindings.add((ClassBinding<T>) binding));
        }
        List<SupplierClassBinding<?>> scb = supplierClassBindings.get(clazz);
        if (scb != null) {
            scb.forEach(binding -> classBindings.supplierClassBindings.add((SupplierClassBinding<T>) binding));
        }
        return classBindings;
    }

    /**
     * Create {@link TypeBindings} instance containing bindings and instances for the given Type.
     * @param type the given type.
     * @param <T> Expected return type.
     * @return the {@link TypeBindings}.
     */
    @SuppressWarnings("unchecked")
    private <T> TypeBindings<T> typeBindings(Type type) {
        TypeBindings<T> typeBindings = new TypeBindings<>(type);
        List<InstanceBinding<?>> ib = instanceTypeBindings.get(type);
        if (ib != null) {
            ib.forEach(binding -> typeBindings.instanceBindings.add((InstanceBinding<T>) binding));
        }
        List<SupplierInstanceBinding<?>> sib = supplierTypeInstanceBindings.get(type);
        if (sib != null) {
            sib.forEach(binding -> typeBindings.supplierInstanceBindings.add((SupplierInstanceBinding<T>) binding));
        }
        List<ClassBinding<?>> cb = contractTypeBindings.get(type);
        if (cb != null) {
            cb.forEach(binding -> typeBindings.classBindings.add((ClassBinding<T>) binding));
        }
        List<SupplierClassBinding<?>> scb = supplierTypeClassBindings.get(type);
        if (scb != null) {
            scb.forEach(binding -> typeBindings.supplierClassBindings.add((SupplierClassBinding<T>) binding));
        }
        return typeBindings;
    }

    /**
     * <p>
     * A class that contains relevant bindings for a given TYPE, filtered from all registered bindings.
     * The TYPE is either Type (ParametrizedType) or Class.
     * </p>
     * <p>
     * The class also filters any bindings for which the singleton or thread-scoped instance already is created.
     * The class either provides the existing instance, or all instances of the TYPE, or {@link ServiceHolder}s.
     * </p>
     * @param <X> The expected return type for the TYPE.
     * @param <TYPE> The Type for which a {@link Binding} has been created.
     */
    private abstract class XBindings<X, TYPE> {

        protected final List<InstanceBinding<X>> instanceBindings = new LinkedList<>();
        protected final List<SupplierInstanceBinding<X>> supplierInstanceBindings = new LinkedList<>();
        protected final List<ClassBinding<X>> classBindings = new LinkedList<>();
        protected final List<SupplierClassBinding<X>> supplierClassBindings = new LinkedList<>();

        protected final TYPE type;
        protected final Annotation[] instancesQualifiers;
        protected final TypedInstances<TYPE> instances;

        protected XBindings(TYPE type, Annotation[] instancesQualifiers, TypedInstances<TYPE> instances) {
            this.type = type;
            this.instancesQualifiers = instancesQualifiers;
            this.instances = instances;
        }

        int size() {
            return instanceBindings.size()
                    + supplierInstanceBindings.size()
                    + classBindings.size()
                    + supplierClassBindings.size();
        }

        private void _checkUnique() {
            if (size() > 1) {
                throw new IllegalStateException(LocalizationMessages.NONINJECT_AMBIGUOUS_SERVICES(type));
            }
        }

        void filterBinding(Binding binding) {
            if (InstanceBinding.class.isInstance(binding)) {
                instanceBindings.remove(binding);
            } else if (ClassBinding.class.isInstance(binding)) {
                classBindings.remove(binding);
            } else if (SupplierInstanceBinding.class.isInstance(binding)) {
                supplierInstanceBindings.remove(binding);
            } else if (SupplierClassBinding.class.isInstance(binding)) {
                supplierClassBindings.remove(binding);
            }
        }

        /**
         * Match the binging qualifiers
         * @param bindingQualifiers the qualifiers registered with the bindings
         */
        void matchQualifiers(Annotation... bindingQualifiers) {
            if (bindingQualifiers != null) {
                _filterRequested(instanceBindings, bindingQualifiers);
                _filterRequested(classBindings, bindingQualifiers);
                _filterRequested(supplierInstanceBindings, bindingQualifiers);
                _filterRequested(supplierClassBindings, bindingQualifiers);
            }
        }

        @SuppressWarnings("unchecked")
        private void _filterRequested(List<? extends Binding<?, ?>> bindingList, Annotation... requestedQualifiers) {
            for (Iterator<? extends Binding> bindingIterator = bindingList.iterator(); bindingIterator.hasNext();) {
                Binding<X, ?> binding = bindingIterator.next();
                classLoop:
                for (Annotation requestedQualifier : requestedQualifiers) {
                    for (Annotation bindingQualifier : binding.getQualifiers()) {
                        if (requestedQualifier.annotationType().isInstance(bindingQualifier)) {
                            continue classLoop;
                        }
                    }
                    bindingIterator.remove();
                }
            }
        }

        protected boolean _isPerThread(Class<? extends Annotation> scope) {
            return RequestScoped.class.equals(scope) || PerThread.class.equals(scope);
        }

        private X _getInstance(InstanceBinding<X> instanceBinding) {
            return instanceBinding.getService();
        }

        private X _create(SupplierInstanceBinding<X> binding) {
            Supplier<X> supplier = binding.getSupplier();
            X t = registerDisposableSupplierAndGet(supplier);
            if (Singleton.class.equals(binding.getScope())) {
                _addInstance(t, binding);
            } else if (_isPerThread(binding.getScope())) {
                _addThreadInstance(t, binding);
            }
            return t;
        }

        X create(boolean throwWhenNoBinding) {
            _checkUnique();
            if (!instanceBindings.isEmpty()) {
                return _getInstance(instanceBindings.get(0));
            } else if (!supplierInstanceBindings.isEmpty()) {
                return _create(supplierInstanceBindings.get(0));
            } else if (!classBindings.isEmpty()) {
                return _createAndStore(classBindings.get(0));
            } else if (!supplierClassBindings.isEmpty()) {
                return _create(supplierClassBindings.get(0));
            }

            if (throwWhenNoBinding) {
                throw new IllegalStateException(LocalizationMessages.NONINJECT_NO_BINDING(type));
            } else {
                return null;
            }
        }

        protected X getInstance() {
            X instance = instances.getInstance(type, instancesQualifiers);
            if (instance != null) {
                return instance;
            }
            return create(true);
        }

        List<X> allInstances() {
            List<X> list = new LinkedList<>();
            List<InstanceContext<?>> instanceContextList;

            instanceContextList = instances.getContexts(type, instancesQualifiers);
            if (instanceContextList != null) {
                instanceContextList.forEach(instanceContext -> filterBinding(instanceContext.getBinding()));
                instanceContextList.forEach(instanceContext -> list.add((X) instanceContext.getInstance()));
            }

            list.addAll(instanceBindings.stream()
                    .map(this::_getInstance)
                    .collect(Collectors.toList()));

            list.addAll(classBindings.stream()
                    .map(this::_createAndStore)
                    .collect(Collectors.toList()));

            list.addAll(supplierInstanceBindings.stream()
                    .map(this::_create)
                    .collect(Collectors.toList()));

            list.addAll(supplierClassBindings.stream()
                    .map(this::_create)
                    .collect(Collectors.toList()));

            return list;
        }

        protected abstract X _create(SupplierClassBinding<X> binding);

        protected abstract X _createAndStore(ClassBinding<X> binding);

        protected <T> T _addInstance(TYPE type, T instance, Binding<?, ?> binding) {
            return instances.addSingleton(type, instance, binding, instancesQualifiers);
        }

        protected void _addThreadInstance(TYPE type, Object instance, Binding binding) {
            instances.addThreadInstance(type, instance, binding, instancesQualifiers);
        }

        protected <T> T _addInstance(T instance, Binding<?, ?> binding) {
            return instances.addSingleton(type, instance, binding, instancesQualifiers);
        }

        protected void _addThreadInstance(Object instance, Binding binding) {
            instances.addThreadInstance(type, instance, binding, instancesQualifiers);
        }
    }


    private class ClassBindings<T> extends XBindings<T, Class<?>> {
        private ClassBindings(Class<T> clazz, Annotation[] instancesQualifiers) {
            super(clazz, instancesQualifiers, NonInjectionManager.this.instances);
        }

        @SuppressWarnings("unchecked")
        List<ServiceHolder<T>> getAllServiceHolders(Annotation... qualifiers) {
            matchQualifiers(qualifiers);

            List<ServiceHolder<T>> holders = new LinkedList<>();
            List<InstanceContext<?>> instanceContextList;

            instanceContextList = instances.getContexts(type, qualifiers);

            if (instanceContextList != null) {
                instanceContextList.forEach(instanceContext -> filterBinding(instanceContext.getBinding()));
                instanceContextList.forEach(instanceContext -> holders.add(new ServiceHolderImpl<T>(
                        (T) instanceContext.getInstance(),
                        (Class<T>) instanceContext.getInstance().getClass(),
                        instanceContext.getBinding().getContracts(),
                        instanceContext.getBinding().getRank() == null ? 0 : instanceContext.getBinding().getRank())
                ));
            }

            List<ServiceHolder<T>> instanceBindingHolders = instanceBindings.stream()
                    .map(this::_serviceHolder)
                    .collect(Collectors.toList());
            holders.addAll(instanceBindingHolders);

            List<ServiceHolder<T>> classBindingHolders = classBindings.stream()
                    .filter(binding -> NonInjectionManager.this.findConstructor(binding.getService()) != null)
                    .map(this::_serviceHolder)
                    .collect(Collectors.toList());
            holders.addAll(classBindingHolders);

            return holders;
        }

        private <T> ServiceHolderImpl<T> _serviceHolder(InstanceBinding<T> binding) {
            return new ServiceHolderImpl<T>(
                    binding.getService(),
                    binding.getImplementationType(),
                    binding.getContracts(),
                    binding.getRank() == null ? 0 : binding.getRank());
        }

        private <T> ServiceHolderImpl<T> _serviceHolder(ClassBinding<T> binding) {
            return new ServiceHolderImpl<T>(
                    NonInjectionManager.this.create(binding.getService()),
                    binding.getImplementationType(),
                    binding.getContracts(),
                    binding.getRank() == null ? 0 : binding.getRank());
        }

        protected T _create(SupplierClassBinding<T> binding) {
            Supplier<T> supplier = instances.getInstance(binding.getSupplierClass(), null);
            if (supplier == null) {
                supplier = justCreate(binding.getSupplierClass());
                if (Singleton.class.equals(binding.getSupplierScope())) {
                    supplier = instances.addSingleton(binding.getSupplierClass(), supplier, binding, null);
                } else if (_isPerThread(binding.getSupplierScope())) {
                    instances.addThreadInstance(binding.getSupplierClass(), supplier, binding, null);
                }
            }

            T t = createSupplierProxyIfNeeded(binding.isProxiable(), (Class<T>) type, supplier);
            if (Singleton.class.equals(binding.getScope())) {
                t = _addInstance(type, t, binding);
            } else if (_isPerThread(binding.getScope())) {
                _addThreadInstance(type, t, binding);
            }
            return t;
        }

        protected T _createAndStore(ClassBinding<T> binding) {
            T result = justCreate(binding.getService());
            result = _addInstance(binding.getService(), result, binding);
            return result;
        }
    }

    private class TypeBindings<T> extends XBindings<T, Type> {
        private TypeBindings(Type type) {
            super(type, null, types);
        }

        protected T _create(SupplierClassBinding<T> binding) {
            Supplier<T> supplier = justCreate(binding.getSupplierClass());

            T t = registerDisposableSupplierAndGet(supplier);
            if (Singleton.class.equals(binding.getScope())) {
                t = _addInstance(type, t, binding);
            } else if (_isPerThread(binding.getScope())) {
                _addThreadInstance(type, t, binding);
            }
            return t;
        }

        @Override
        protected T _createAndStore(ClassBinding<T> binding) {
            T result = justCreate(binding.getService());
            result = _addInstance(type, result, binding);
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        T create(boolean throwWhenNoBinding) {
            if (ParameterizedType.class.isInstance(type)) {
                ParameterizedType pt = (ParameterizedType) type;
                if (Provider.class.equals(pt.getRawType())) {
                    return (T) new Provider<Object>() {
                        final SingleRegisterSupplier<Object> supplier = new SingleRegisterSupplier<>(new Supplier<Object>() {
                            @Override
                            public Object get() {
                                Type actualTypeArgument = pt.getActualTypeArguments()[0];
                                if (isClass(actualTypeArgument)) {
                                    return NonInjectionManager.this.getInstance((Class<? extends T>) actualTypeArgument);
                                } else {
                                    return NonInjectionManager.this.getInstance(actualTypeArgument);
                                }
                            }
                        });

                        @Override
                        public Object get() {
                            return supplier.get(); //Not disposable
                        }
                    };
                }
            }
            return super.create(throwWhenNoBinding);
        }
    }

    /**
     * A triplet of created instance, the registered {@link Binding} that prescribed the creation of the instance
     * and {@link Annotation qualifiers} the instance was created with.
     * @param <T> type of the instance.
     * @see NonInjectionManager#getInstance(Class, Annotation[])
     */
    private static class InstanceContext<T> {
        private final T instance;
        private final Binding<?, ?> binding;
        private final Annotation[] createdWithQualifiers;

        private InstanceContext(T instance, Binding<?, ?> binding, Annotation[] qualifiers) {
            this.instance = instance;
            this.binding = binding;
            this.createdWithQualifiers = qualifiers;
        }

        public Binding<?, ?> getBinding() {
            return binding;
        }

        public T getInstance() {
            return instance;
        }

        @SuppressWarnings("unchecked")
        static <T> List<T> toInstances(List<InstanceContext<?>> instances, Annotation[] qualifiers) {
            return instances != null
                    ? instances.stream()
                        .filter(instance -> instance.hasQualifiers(qualifiers))
                        .map(pair -> (T) pair.getInstance())
                        .collect(Collectors.toList())
                    : null;
        }

        private static List<InstanceContext<?>> filterInstances(List<InstanceContext<?>> instances, Annotation... qualifiers) {
            return instances != null
                    ? instances.stream()
                        .filter(instance -> instance.hasQualifiers(qualifiers))
                        .collect(Collectors.toList())
                    : null;
        }

        private boolean hasQualifiers(Annotation[] requested) {
            if (requested != null) {
                classLoop:
                for (Annotation req : requested) {
                    if (createdWithQualifiers != null) {
                        for (Annotation cur : createdWithQualifiers) {
                            if (cur.annotationType().isInstance(req)) {
                                continue classLoop;
                            }
                        }
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Singleton Binding this {@link NonInjectionManager} was supposed to be created based upon.
     */
    private static final class InjectionManagerBinding extends Binding<InjectionManager, Binding<?, ?>> {
    }

}
