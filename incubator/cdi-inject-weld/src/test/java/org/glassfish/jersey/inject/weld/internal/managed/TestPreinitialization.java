/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Singleton;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.innate.inject.InternalBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.process.internal.RequestScoped;

public class TestPreinitialization implements org.glassfish.jersey.innate.BootstrapPreinitialization {

    @Override
    public void preregister(RuntimeType runtimeType, InjectionManager injectionManager) {
        injectionManager.register(new InternalBinder() {
            @Override
            protected void configure() {
                if (RuntimeType.CLIENT == runtimeType) {
                    //ClientInstanceInjectionTest
                    {
                        bindFactory(ClientInstanceInjectionTest.InjectableClientServerSupplierClient.class)
                                .to(ClientInstanceInjectionTest.InjectableClientServer.class);
                        bindAsContract(ClientInstanceInjectionTest.InjectedBean.class);
                    }
                    return;
                }

                // Disposable supplier test
                {
                    bindFactory(DisposableSupplierTest.DisposableSupplierImpl.class, Singleton.class)
                            .to(DisposableSupplierTest.StringForSupplierSingletonClass.class);
                    bindFactory(DisposableSupplierTest.DisposableSupplierImpl.class)
                            .to(DisposableSupplierTest.StringForSupplierClass.class);
                    bindFactory(new DisposableSupplierTest.DisposableSupplierImpl())
                            .to(DisposableSupplierTest.StringForSupplierInstance.class);
                    bindFactory(SupplierGreeting.class)
                            .to(DisposableSupplierTest.GreetingsClass.class);
                    bindFactory(new SupplierGreeting())
                            .to(DisposableSupplierTest.GreetingsInstance.class);

                    bindFactory(DisposableSupplierTest.ProxiableDisposableSingletonSupplierImpl.class, Singleton.class)
                            .to(DisposableSupplierTest.ProxiableHolderSingletonClass.class)
                            .in(RequestScoped.class);
                    bindFactory(DisposableSupplierTest.ProxiableDisposableSupplierImpl.class)
                            .to(DisposableSupplierTest.ProxiableHolderClass.class)
                            .in(RequestScoped.class);

                    bindFactory(DisposableSupplierTest.DisposableSupplierForComposedImpl.class, Singleton.class)
                            .to(DisposableSupplierTest.StringForComposed.class);
                    bindAsContract(DisposableSupplierTest.ComposedObject.class)
                            .in(RequestScoped.class);
                }

                // ThreadScopeTest
                {
                    //testThreadScopedInDifferentThread
                    bindAsContract(ThreadScopeTest.SingletonObject.class)
                            .in(Singleton.class);
                    bindFactory(new ThreadScopeTest.SupplierGreeting())
                            .to(ThreadScopeTest.Greeting.class)
                            .in(PerThread.class);

                    //testThreadScopedInRequestScope
                    bindAsContract(ThreadScopeTest.RequestScopedInterface.class)
                            .in(jakarta.enterprise.context.RequestScoped.class);
//                    bindFactory(new SupplierGreeting())
//                            .to(Greeting.class)
//                            .in(PerThread.class);

                    //testThreadScopedInRequestScopeImplementation
                    bindAsContract(ThreadScopeTest.RequestScopedCzech.class)
                            .in(jakarta.enterprise.context.RequestScoped.class);
                    bindFactory(new ThreadScopeTest.SupplierGreeting())
                            .to(ThreadScopeTest.CzechGreeting.class)
                            .in(PerThread.class);

                    //testThreadScopedInRequestTwoTypes
                    bindAsContract(ThreadScopeTest.RequestScopedCzech2.class)
                            .in(jakarta.enterprise.context.RequestScoped.class);
                    bindAsContract(ThreadScopeTest.RequestScopedEnglish2.class)
                            .in(jakarta.enterprise.context.RequestScoped.class);
                    bindFactory(new ThreadScopeTest.SupplierGreeting2(ThreadScopeTest.CzechGreeting2.GREETING))
                            .to(ThreadScopeTest.CzechGreeting2.class)
                            .in(PerThread.class);
                    bindFactory(new ThreadScopeTest.SupplierGreeting2(ThreadScopeTest.EnglishGreeting2.GREETING))
                            .to(ThreadScopeTest.EnglishGreeting2.class)
                            .in(PerThread.class);
                    //testSupplierClassBindingThreadScopedInSingletonScope
                    bindAsContract(ThreadScopeTest.SingletonObject3.class)
                            .in(Singleton.class);
                    bindFactory(ThreadScopeTest.SupplierGreeting3.class)
                            .to(ThreadScopeTest.Greeting3.class)
                            .in(PerThread.class);
                }

                //ClientInstanceInjectionTest
                {
                    bind(new ClientInstanceInjectionTest.StringInjectable(0))
                            .to(ClientInstanceInjectionTest.Injectable.class).in(Dependent.class);
                    bindAsContract(ClientInstanceInjectionTest.InjectedBean.class);

                    bindFactory(new ClientInstanceInjectionTest.StringInjectableSupplier2(0))
                            .to(ClientInstanceInjectionTest.Injectable2.class).in(Dependent.class);
                    //bindAsContract(ClientInstanceInjectionTest.InjectedSupplierBean.class);

                    bindFactory(ClientInstanceInjectionTest.InjectableClientServerSupplierServer.class)
                            .to(ClientInstanceInjectionTest.InjectableClientServer.class);
                }
            }
        });
    }
}
