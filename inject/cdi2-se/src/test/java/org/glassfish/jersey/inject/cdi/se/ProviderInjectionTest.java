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

package org.glassfish.jersey.inject.cdi.se;

import javax.ws.rs.core.Context;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProviderInjectionTest {

    private InjectionManager injectionManager;

    @BeforeEach
    public void setup() {
        injectionManager = BindingTestHelper.createInjectionManager();
    }

    @AfterEach
    public void teardown() {
        injectionManager.shutdown();
    }

    @Test
    public void testProviderInject() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bind(CzechGreeting.class).to(Greeting.class);
            binder.bindAsContract(ProviderInject.class);
        });

        ProviderInject instance = injectionManager.getInstance(ProviderInject.class);
        assertThat(instance.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testProviderContext() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bind(CzechGreeting.class).to(Greeting.class);
            binder.bindAsContract(ProviderContext.class);
        });

        ProviderContext instance = injectionManager.getInstance(ProviderContext.class);
        assertThat(instance.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    @Test
    public void testProviderFactoryInject() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
            binder.bindAsContract(ProviderInject.class);
        });

        ProviderInject conversation = injectionManager.getInstance(ProviderInject.class);
        assertThat(conversation.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }


    @Test
    public void testProviderFactoryContext() {
        BindingTestHelper.bind(injectionManager, binder -> {
            binder.bindFactory(SupplierGreeting.class).to(Greeting.class);
            binder.bindAsContract(ProviderContext.class);
        });

        ProviderContext conversation = injectionManager.getInstance(ProviderContext.class);
        assertThat(conversation.greeting.get().getGreeting(), StringStartsWith.startsWith(CzechGreeting.GREETING));
    }

    public static class ProviderInject {
        @Inject
        Provider<Greeting> greeting;
    }

    public static class ProviderContext {
        @Context
        Provider<Greeting> greeting;
    }

}
