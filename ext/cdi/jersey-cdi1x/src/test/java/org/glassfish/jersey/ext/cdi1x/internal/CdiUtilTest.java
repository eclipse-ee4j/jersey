/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;

import org.glassfish.jersey.ext.cdi1x.internal.spi.BeanManagerProvider;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.internal.inject.InjectionManager;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;

/**
 * Unit tests for {@link org.glassfish.jersey.ext.cdi1x.internal.CdiUtil}.
 *
 * @author Michal Gajdos
 */
public class CdiUtilTest {

    public static class TestBeanManagerProvider implements BeanManagerProvider {

        @Override
        public BeanManager getBeanManager() {
            throw new RuntimeException("BeanManager!");
        }
    }

    @Test
    public void getBeanManagerCustom(@Mocked final TestBeanManagerProvider custom,
                                     @Mocked final DefaultBeanManagerProvider fallback) throws Exception {
        CdiUtil.getBeanManager();

        new Verifications() {{
            custom.getBeanManager(); times = 1;
            fallback.getBeanManager(); times = 0;
        }};
    }

    @Test
    public void getDefaultBeanManagerDefault(@Mocked final DefaultBeanManagerProvider fallback) throws Exception {
        new MockUp<CdiUtil>() {
            @Mock
            @SuppressWarnings("UnusedDeclaration")
            <T> T lookupService(final Class<T> clazz) {
                return null;
            }
        };

        CdiUtil.getBeanManager();

        new Verifications() {{
            fallback.getBeanManager(); times = 1;
        }};
    }

    @Priority(500)
    public static class MyServiceOne implements MyService {
    }

    @Priority(100)
    public static class MyServiceTwo implements MyService {
    }

    @Priority(300)
    public static class MyServiceThree implements MyService {
    }

    @Test
    public void testLookupService() throws Exception {
        assertThat(CdiUtil.lookupService(MyService.class), instanceOf(MyServiceTwo.class));
    }

    @Test
    public void testLookupServiceNegative() throws Exception {
        assertThat(CdiUtil.lookupService(CdiUtil.class), nullValue());
    }

    public static class TestInjectionManagerStore implements InjectionManagerStore {

        @Override
        public void registerInjectionManager(final InjectionManager injectionManager) {
        }

        @Override
        public InjectionManager getEffectiveInjectionManager() {
            return null;
        }
    }

    @Test
    public void createHk2LocatorManagerCustom() throws Exception {
        assertThat(CdiUtil.createHk2InjectionManagerStore(), instanceOf(TestInjectionManagerStore.class));
    }

    @Test
    public void createHk2LocatorManagerDefault() throws Exception {
        new MockUp<CdiUtil>() {
            @Mock
            @SuppressWarnings("UnusedDeclaration")
            <T> T lookupService(final Class<T> clazz) {
                return null;
            }
        };

        assertThat(CdiUtil.createHk2InjectionManagerStore(), instanceOf(SingleInjectionManagerStore.class));
    }
}
