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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.managed.CdiInjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ClientInstanceInjectionTest extends TestParent {

    @Test
    public void testInject() {
        InjectionManager clientInjectionManager1 = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);
        InjectionManager clientInjectionManager2 = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);

        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bind(new StringInjectable(5)).to(Injectable.class);
        });

        BindingTestHelper.bind(clientInjectionManager1, binder -> {
            binder.bind(new StringInjectable(10)).to(Injectable.class);
        });

        BindingTestHelper.bind(clientInjectionManager2, binder -> {
            binder.bind(new StringInjectable(15)).to(Injectable.class);
        });

        InjectedBean bean = injectionManager.getInstance(InjectedBean.class);
        Assertions.assertNotNull(bean);
        Assertions.assertEquals("6", bean.get());

        InjectedBean bean1 = clientInjectionManager1.getInstance(InjectedBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertEquals("11", bean1.get());

        InjectedBean bean2 = clientInjectionManager2.getInstance(InjectedBean.class);
        Assertions.assertNotNull(bean2);
        Assertions.assertEquals("16", bean2.get());

        injectionManager.shutdown();
        clientInjectionManager1.shutdown();
        clientInjectionManager2.shutdown();
    }

    @Vetoed
    static class InjectedBean {
        @Inject
        private Injectable injectable;

        public String get() {
            return injectable.get();
        }
    }

    static class StringInjectable extends AbstractStringInjectable implements Injectable {
        StringInjectable(int i) {
            super(i);
        }
    }

    static interface Injectable {
        String get();
    }


    @Test
    public void testSupplierInject() {
        InjectionManager clientInjectionManager1 = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);
        InjectionManager clientInjectionManager2 = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);

        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(new StringInjectableSupplier2(5)).to(Injectable2.class);
        });

        BindingTestHelper.bind(clientInjectionManager1, binder -> {
            binder.bindFactory(new StringInjectableSupplier2(10)).to(Injectable2.class);
        });

        BindingTestHelper.bind(clientInjectionManager2, binder -> {
            binder.bindFactory(new StringInjectableSupplier2(15)).to(Injectable2.class);
        });

        InjectedSupplierBean bean = injectionManager.getInstance(InjectedSupplierBean.class);
        Assertions.assertNotNull(bean);
        Assertions.assertEquals("6", bean.get());

        InjectedSupplierBean bean1 = clientInjectionManager1.getInstance(InjectedSupplierBean.class);
        Assertions.assertNotNull(bean1);
        Assertions.assertEquals("11", bean1.get());

        InjectedSupplierBean bean2 = clientInjectionManager2.getInstance(InjectedSupplierBean.class);
        Assertions.assertNotNull(bean2);
        Assertions.assertEquals("16", bean2.get());
    }

    @Dependent
    static class InjectedSupplierBean {
        @Inject
        private Supplier<Injectable2> injectable;

        public String get() {
            return injectable.get().get();
        }
    }

    static class StringInjectableSupplier2 implements Supplier<Injectable2> {
        private final int i;
        StringInjectableSupplier2(int i) {
            this.i = i;
        }

        @Override
        public Injectable2 get() {
            return new StringInjectable2(i);
        }
    }

    private static class StringInjectable2 extends AbstractStringInjectable implements Injectable2 {

        StringInjectable2(int value) {
            super(value);
        }
    }

    static interface Injectable2 {
        String get();
    }

    abstract static class AbstractStringInjectable {
        protected AtomicInteger atomicInteger = new AtomicInteger(0);

        AbstractStringInjectable(int value) {
            atomicInteger.set(value);
        }

        public String get() {
            return String.valueOf(atomicInteger.incrementAndGet());
        }
    }

    @Test
    public void testSupplierClassBeanOnClientAndServer() {
        InjectionManager clientInjectionManager = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(InjectableClientServerSupplierServer.class).to(InjectableClientServer.class);
        });

        BindingTestHelper.bind(clientInjectionManager, binder -> {
            binder.bindFactory(InjectableClientServerSupplierClient.class).to(InjectableClientServer.class);
        });

        InjectedClientServerSupplierBean beanServer = injectionManager.getInstance(InjectedClientServerSupplierBean.class);
        Assertions.assertNotNull(beanServer);
        Assertions.assertEquals("SERVER", beanServer.get());

        InjectedClientServerSupplierBean beanClient = clientInjectionManager.getInstance(InjectedClientServerSupplierBean.class);
        Assertions.assertNotNull(beanClient);
        Assertions.assertEquals("CLIENT", beanClient.get());
    }

    @Dependent
    static class InjectedClientServerSupplierBean {
        @Inject
        private Supplier<InjectableClientServer> injectable;

        public String get() {
            return injectable.get().get();
        }
    }

    static class InjectableClientServerSupplierServer implements Supplier<InjectableClientServer> {
        @Override
        public InjectableClientServer get() {
            return new InjectableClientServerServer();
        }
    }

    static class InjectableClientServerSupplierClient implements Supplier<InjectableClientServer> {
        @Override
        public InjectableClientServer get() {
            return new InjectableClientServerClient();
        }
    }

    private static class InjectableClientServerServer implements InjectableClientServer {

        @Override
        public String get() {
            return "SERVER";
        }
    }

    private static class InjectableClientServerClient implements InjectableClientServer {
        @Override
        public String get() {
            return "CLIENT";
        }
    }

    static interface InjectableClientServer {
        String get();
    }
}
