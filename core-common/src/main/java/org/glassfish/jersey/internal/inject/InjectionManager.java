/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Interface provides the communication API between Jersey and Dependency Injection provider.
 * <p>
 * Lifecycle methods should be called in this order:
 * <ul>
 * <li>{@link #completeRegistration()} - notifies that Jersey bootstrap has been finished and DI provider should be ready for a runtime.</li>
 * <li>{@link #shutdown()} - Jersey application has been closed and DI provider should make needed cleaning steps.</li>
 * </ul>
 * <p>
 * All {@code getInstance} methods can be called after {@link #completeRegistration()} method has been called because at this all
 * components are bound to injection manager and ready for getting.
 * In turn, {@link #shutdown()} method stops the possibility to use these methods and closes {@code InjectionManager}.
 *
 * @author Petr Bouda
 */
public interface InjectionManager {

    /**
     * Completes {@link InjectionManager} and the underlying DI provider. All registered components are bound to injection
     * manager and after an invocation of this method all components are available using e.g. {@link #getInstance(Class)}.
     */
    void completeRegistration();

    /**
     * Shuts down the entire {@link InjectionManager} and the underlying DI provider.
     * <p>
     * Shutdown phase is dedicated to make some final cleaning steps regarding underlying DI provider.
     */
    void shutdown();

    /**
     * Returns {@code true} when the {@link InjectionManager} has been shutdown, {@code false} otherwise.
     * @return Whether the {@code InjectionManager} has been shutdown.
     */
    boolean isShutdown();

    /**
     * Registers one bean represented using fields in the provided descriptor. The final bean can be direct bean or
     * factory object which will create the bean at the time of injection. {@code InjectionManager} is able to register a bean
     * represented by a class or direct instance.
     *
     * @param binding one descriptor.
     * @see ClassBinding
     * @see InstanceBinding
     * @see SupplierClassBinding
     * @see SupplierInstanceBinding
     */
    void register(Binding binding);

    /**
     * Registers a collection of beans represented using fields in the provided descriptors. The final bean can be
     * direct bean or factory object which will create the bean at the time of injection. {@code InjectionManager} is able to
     * register a bean represented by a class or direct instance.
     *
     * @param descriptors collection of descriptors.
     * @see ClassBinding
     * @see InstanceBinding
     * @see SupplierClassBinding
     * @see SupplierInstanceBinding
     */
    void register(Iterable<Binding> descriptors);

    /**
     * Registers beans which are included in {@link Binder}. {@code Binder} can contains all descriptors extending
     * {@link Binding} or other binders which are installed together in tree-structure. This method will get all descriptors
     * bound in the given binder and register them in the order how the binders are installed together. In the tree structure,
     * the deeper on the left side will be processed first.
     *
     * @param binder collection of descriptors.
     * @see ClassBinding
     * @see InstanceBinding
     * @see SupplierClassBinding
     * @see SupplierInstanceBinding
     */
    void register(Binder binder);

    /**
     * Registers a provider. An implementation of the {@link InjectionManager} should test whether the type of the object can be
     * registered using the method {@link #isRegistrable(Class)}. Then a caller has an certainty that the instance of the tested
     * class can be registered in {@code InjectionManager}. If {@code InjectionManager} is not able to register the provider
     * then {@link IllegalArgumentException} is thrown.
     *
     * @param provider object that can be registered in {@code InjectionManager}.
     * @throws IllegalArgumentException provider cannot be registered.
     */
    void register(Object provider) throws IllegalArgumentException;

    /**
     * Tests whether the provided {@code clazz} can be registered by the implementation of the {@link InjectionManager}.
     *
     * @param clazz type that is tested whether is registrable by the implementation of {@code InjectionManager}.
     * @return {@code true} if the {@code InjectionManager} is able to register this type.
     */
    boolean isRegistrable(Class<?> clazz);

    /**
     * Creates an object with the given class.
     * <p>
     * The object created is not managed by the injection manager.
     *
     * @param createMe The non-null class to create this object from;
     * @return An instance of the object that has been created.
     * @since 2.35
     */
    <T> T create(Class<T> createMe);

    /**
     * Creates, injects and post-constructs an object with the given class. This is equivalent to calling the
     * {@code create-class} method followed by the {@code inject-class} method followed by the {@code post-construct} method.
     * <p>
     * The object created is not managed by the injection manager.
     *
     * @param createMe The non-null class to create this object from;
     * @return An instance of the object that has been created, injected and post constructed.
     */
    <T> T createAndInitialize(Class<T> createMe);

    /**
     * Gets all services from this injection manager that implements this contract or has this implementation along with
     * information about the service which can be kept by {@link ServiceHolder}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param qualifiers     The set of qualifiers that must match this service definition.
     * @param <T>            Instance type.
     * @return An instance of the contract or impl along with other information. May return  null if there is no provider that
     * provides the given implementation or contract.
     */
    <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers);

    /**
     * Gets the best service from this injection manager that implements this contract or has this implementation.
     * <p>
     * Use this method only if other information is not needed otherwise use, otherwise use
     * {@link InjectionManager#getAllServiceHolders(Class, Annotation...)}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param qualifiers     The set of qualifiers that must match this service definition.
     * @param <T>            Instance type.
     * @return An instance of the contract or impl.  May return  null if there is no provider that provides the given
     * implementation or contract.
     */
    <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers);

    /**
     * Gets the best service from this injection manager that implements this contract or has this implementation.
     * <p>
     * Use this method only if other information is not needed otherwise use, otherwise use
     * {@link InjectionManager#getAllServiceHolders(Class, Annotation...)}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param classAnalyzer  -------
     * @param <T>            Instance type.
     * @return An instance of the contract or impl.  May return  null if there is no provider that provides the given
     * implementation or contract.
     */
    // TODO: Remove CLASS ANALYZER - NEEDED ONLY IN CdiComponentProvider
    <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer);

    /**
     * Gets the best service from this injection manager that implements this contract or has this implementation.
     * <p>
     * Use this method only if other information is not needed otherwise use, otherwise use
     * {@link InjectionManager#getAllServiceHolders(Class, Annotation...)}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param <T>            Instance type.
     * @return An instance of the contract or impl.  May return  null if there is no provider that provides the given
     * implementation or contract.
     */
    <T> T getInstance(Class<T> contractOrImpl);

    /**
     * Gets the best service from this injection manager that implements this contract or has this implementation.
     * <p>
     * Use this method only if other information is not needed otherwise use, otherwise use
     * {@link InjectionManager#getAllServiceHolders(Class, Annotation...)}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param <T>            Instance type.
     * @return An instance of the contract or impl.  May return  null if there is no provider that provides the given
     * implementation or contract.
     */
    <T> T getInstance(Type contractOrImpl);

    /**
     * Gets the service instance according to {@link ForeignDescriptor} which is specific to the underlying DI provider.
     *
     * @param foreignDescriptor DI specific descriptor.
     * @return service instance according to foreign descriptor.
     */
    Object getInstance(ForeignDescriptor foreignDescriptor);

    /**
     * Creates and registers the descriptor in the underlying DI provider and returns {@link ForeignDescriptor} that is specific
     * descriptor for the underlying DI provider.
     *
     * @param binding jersey descriptor.
     * @return specific foreign descriptor of the underlying DI provider.
     */
    ForeignDescriptor createForeignDescriptor(Binding binding);

    /**
     * Gets all services from this injection manager that implement this contract or have this implementation.
     * <p>
     * Use this method only if other information is not needed otherwise use, otherwise use
     * {@link InjectionManager#getAllServiceHolders(Class, Annotation...)}.
     *
     * @param contractOrImpl May not be null, and is the contract or concrete implementation to get the best instance of.
     * @param <T>            Instance type.
     * @return A list of services implementing this contract or concrete implementation.  May not return null, but  may return
     * an empty list
     */
    <T> List<T> getAllInstances(Type contractOrImpl);

    /**
     * Analyzes the given object and inject into its fields and methods.
     * The object injected in this way will not be managed by HK2
     *
     * @param injectMe The object to be analyzed and injected into
     */
    void inject(Object injectMe);

    /**
     * This will analyze the given object and inject into its fields and methods. The object injected in this way will not be
     * managed by HK2
     *
     * @param injectMe The object to be analyzed and injected into
     */
    // TODO: Remove CLASS ANALYZER - only in legacy CDI integration.
    void inject(Object injectMe, String classAnalyzer);

    /**
     * Analyzes the given object and call the preDestroy method. The object given will not be managed by bean manager.
     *
     * @param preDestroyMe The object to preDestroy
     */
    void preDestroy(Object preDestroyMe);
}
