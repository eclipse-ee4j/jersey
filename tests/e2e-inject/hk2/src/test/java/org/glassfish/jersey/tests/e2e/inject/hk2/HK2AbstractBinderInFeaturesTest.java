/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test that HK2 binder allows for injection into a feature
 */
public class HK2AbstractBinderInFeaturesTest extends JerseyTest {

    private static final AtomicInteger binderCounter = new AtomicInteger();
    private static final AtomicInteger feature1Counter = new AtomicInteger();
    private static final AtomicInteger feature2Counter = new AtomicInteger();
    private static final String VALUE = "CONFIGURED_VALUE";

    public static class InjectableHK2Binder extends org.glassfish.hk2.utilities.binding.AbstractBinder {
        @Override
        protected void configure() {
            binderCounter.incrementAndGet();
            bindAsContract(ConfigurableInjectable.class).to(Injectable.class).in(Singleton.class);
        }
    }

    public static class JerseyInjectableHK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bindAsContract(ConfigurableInjectable.class).to(ExtendedInjectable.class).in(Singleton.class);
        }
    }

    public static final class InjectableHK2BindingFeature implements Feature {
        private final Injectable service;
        private final ExtendedInjectable extendedService;

        @Inject
        public InjectableHK2BindingFeature(Injectable service, ExtendedInjectable extendedService) {
            feature1Counter.incrementAndGet();
            this.service = service;
            this.extendedService = extendedService;
        }

        @Override
        public boolean configure(FeatureContext context) {
            if (service != null) {
                ((ConfigurableInjectable) service).set(VALUE);
            }
            if (extendedService != null) {
                feature2Counter.incrementAndGet();
            }
            return true;
        }
    }

    public static class ConfigurableInjectable implements ExtendedInjectable {
        private String value;
        public void set(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static interface ExtendedInjectable extends Injectable {
    };

    @Override
    protected Application configure() {
        return new ResourceConfig(InjectableHK2BindingFeature.class, AbstractBinderTestResource.class,
                InjectableTestFilter.class, InjectableHK2Binder.class).register(new JerseyInjectableHK2Binder());
    }

    @Test
    public void testInjectableInjection() {
        String response = target().request().get(String.class);
        assertThat(response, is(VALUE));
        assertThat(1, is(binderCounter.get()));
        assertThat(1, is(feature1Counter.get()));
        assertThat(1, is(feature2Counter.get()));
    }
}
