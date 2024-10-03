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

import org.glassfish.jersey.inject.weld.spi.BootstrapPreinitialization;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.process.internal.RequestScoped;

public class TestPreinitialization implements BootstrapPreinitialization {
    @Override
    public void register(RuntimeType runtimeType, AbstractBinder binder) {
        if (RuntimeType.CLIENT == runtimeType) {
            //ClientInstanceInjectionTest
            {
                binder.bindFactory(ClientInstanceInjectionTest.InjectableClientServerSupplierClient.class)
                        .to(ClientInstanceInjectionTest.InjectableClientServer.class);
            }
            return;
        }

        // Disposable supplier test
        {
            binder.bindFactory(DisposableSupplierTest.DisposableSupplierImpl.class, Singleton.class)
                    .to(DisposableSupplierTest.StringForSupplierSingletonClass.class);
            binder.bindFactory(DisposableSupplierTest.DisposableSupplierImpl.class)
                    .to(DisposableSupplierTest.StringForSupplierClass.class);
            binder.bindFactory(new DisposableSupplierTest.DisposableSupplierImpl())
                    .to(DisposableSupplierTest.StringForSupplierInstance.class);
            binder.bindFactory(SupplierGreeting.class)
                    .to(DisposableSupplierTest.GreetingsClass.class);
            binder.bindFactory(new SupplierGreeting())
                    .to(DisposableSupplierTest.GreetingsInstance.class);

            binder.bindFactory(DisposableSupplierTest.ProxiableDisposableSingletonSupplierImpl.class, Singleton.class)
                    .to(DisposableSupplierTest.ProxiableHolderSingletonClass.class)
                    .in(RequestScoped.class);
            binder.bindFactory(DisposableSupplierTest.ProxiableDisposableSupplierImpl.class)
                    .to(DisposableSupplierTest.ProxiableHolderClass.class)
                    .in(RequestScoped.class);

            binder.bindFactory(DisposableSupplierTest.DisposableSupplierForComposedImpl.class, Singleton.class)
                    .to(DisposableSupplierTest.StringForComposed.class);
            binder.bindAsContract(DisposableSupplierTest.ComposedObject.class)
                    .in(RequestScoped.class);
        }

        // ThreadScopeTest
        {
            //testThreadScopedInDifferentThread
            binder.bindAsContract(ThreadScopeTest.SingletonObject.class)
                    .in(Singleton.class);
            binder.bindFactory(new ThreadScopeTest.SupplierGreeting())
                    .to(ThreadScopeTest.Greeting.class)
                    .in(PerThread.class);

            //testThreadScopedInRequestScope
            binder.bindAsContract(ThreadScopeTest.RequestScopedInterface.class)
                    .in(jakarta.enterprise.context.RequestScoped.class);
//                    bindFactory(new SupplierGreeting())
//                            .to(Greeting.class)
//                            .in(PerThread.class);

            //testThreadScopedInRequestScopeImplementation
            binder.bindAsContract(ThreadScopeTest.RequestScopedCzech.class)
                    .in(jakarta.enterprise.context.RequestScoped.class);
            binder.bindFactory(new ThreadScopeTest.SupplierGreeting())
                    .to(ThreadScopeTest.CzechGreeting.class)
                    .in(PerThread.class);

            //testThreadScopedInRequestTwoTypes
            binder.bindAsContract(ThreadScopeTest.RequestScopedCzech2.class)
                    .in(jakarta.enterprise.context.RequestScoped.class);
            binder.bindAsContract(ThreadScopeTest.RequestScopedEnglish2.class)
                    .in(jakarta.enterprise.context.RequestScoped.class);
            binder.bindFactory(new ThreadScopeTest.SupplierGreeting2(ThreadScopeTest.CzechGreeting2.GREETING))
                    .to(ThreadScopeTest.CzechGreeting2.class)
                    .in(PerThread.class);
            binder.bindFactory(new ThreadScopeTest.SupplierGreeting2(ThreadScopeTest.EnglishGreeting2.GREETING))
                    .to(ThreadScopeTest.EnglishGreeting2.class)
                    .in(PerThread.class);

            //testSupplierClassBindingThreadScopedInSingletonScope
            binder.bindAsContract(ThreadScopeTest.SingletonObject3.class)
                    .in(Singleton.class);
            binder.bindFactory(ThreadScopeTest.SupplierGreeting3.class)
                    .to(ThreadScopeTest.Greeting3.class)
                    .in(PerThread.class);
        }

        //ClientInstanceInjectionTest
        {
            binder.bind(new ClientInstanceInjectionTest.StringInjectable(0))
                    .to(ClientInstanceInjectionTest.Injectable.class).in(Dependent.class);
            binder.bindAsContract(ClientInstanceInjectionTest.InjectedBean.class);

            binder.bindFactory(new ClientInstanceInjectionTest.StringInjectableSupplier2(0))
                    .to(ClientInstanceInjectionTest.Injectable2.class).in(Dependent.class);
            //bindAsContract(ClientInstanceInjectionTest.InjectedSupplierBean.class);

            binder.bindFactory(ClientInstanceInjectionTest.InjectableClientServerSupplierServer.class)
                    .to(ClientInstanceInjectionTest.InjectableClientServer.class);
        }
    }
}
