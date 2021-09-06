/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.hk2;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for older hk2 binders to be used
 */
public class HK2AbstractBinderTest extends JerseyTest {

    private static final AtomicInteger counter = new AtomicInteger();

    public static class InjectableHK2Binder extends org.glassfish.hk2.utilities.binding.AbstractBinder {
        @Override
        protected void configure() {
            counter.incrementAndGet();
            bindAsContract(InjectableImpl.class).to(Injectable.class).in(Singleton.class);
        }
    }

    public static class HK2BindingFeature implements Feature {
        @Override
        public boolean configure(FeatureContext context) {
            context.register(new InjectableHK2Binder());
            return true;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(HK2BindingFeature.class, AbstractBinderTestResource.class, InjectableTestFilter.class);
    }

    @Test
    public void testInjectableInjection() {
        String response = target().request().get(String.class);
        assertThat(response, is(InjectableImpl.class.getName()));
        assertThat(1, is(counter.get()));
    }

}
