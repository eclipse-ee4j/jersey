/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.process.internal;

import java.lang.reflect.Type;

import org.glassfish.jersey.inject.hk2.Hk2RequestScope;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.process.internal.RequestScope;

import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of the {@link RequestScope request scope}.
 *
 * @author Miroslav Fuksa
 */
public class RequestScopeTest {

    @Test
    public void testScopeWithCreatedInstance() {
        final RequestScope requestScope = new Hk2RequestScope();
        assertNull(requestScope.suspendCurrent());
        final Hk2RequestScope.Instance context = (Hk2RequestScope.Instance) requestScope.createContext();
        ForeignDescriptor inhab = ForeignDescriptor.wrap(new TestProvider("a"));
        context.put(inhab, "1");
        requestScope.runInScope(context, () -> {
            assertEquals("1", context.get(inhab));
            context.release();
            assertEquals("1", context.get(inhab));
        });
        assertNull(context.get(inhab));
    }

    @Test
    public void testScopeReleaseInsideScope() {
        final RequestScope requestScope = new Hk2RequestScope();
        assertNull(requestScope.suspendCurrent());
        final Hk2RequestScope.Instance instance = (Hk2RequestScope.Instance) requestScope.createContext();
        ForeignDescriptor inhab = ForeignDescriptor.wrap(new TestProvider("a"));
        instance.put(inhab, "1");
        requestScope.runInScope(instance, () -> {
            final Hk2RequestScope.Instance internalInstance = (Hk2RequestScope.Instance) requestScope.suspendCurrent();
            assertEquals(internalInstance, instance);
            assertEquals("1", instance.get(inhab));
            instance.release();
            assertEquals("1", instance.get(inhab));
        });
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertNull(instance.get(inhab));
    }

    @Test
    public void testScopeWithImplicitInstance() throws Exception {
        final RequestScope requestScope = new Hk2RequestScope();
        assertNull(requestScope.suspendCurrent());
        ForeignDescriptor inhab = ForeignDescriptor.wrap(new TestProvider("a"));
        final Hk2RequestScope.Instance instance = requestScope.runInScope(() -> {
            final Hk2RequestScope.Instance internalInstance = (Hk2RequestScope.Instance) requestScope.suspendCurrent();
            assertNull(internalInstance.get(inhab));
            internalInstance.put(inhab, "1");
            assertEquals("1", internalInstance.get(inhab));
            return internalInstance;
        });
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertNull(instance.get(inhab));
    }

    @Test
    public void testScopeWithTwoInternalTasks() throws Exception {
        final RequestScope requestScope = new Hk2RequestScope();
        assertNull(requestScope.suspendCurrent());
        ForeignDescriptor inhab = ForeignDescriptor.wrap(new TestProvider("a"));
        final Hk2RequestScope.Instance instance = requestScope.runInScope(() -> {
            final Hk2RequestScope.Instance internalInstance = (Hk2RequestScope.Instance) requestScope.suspendCurrent();

            final Hk2RequestScope.Instance anotherInstance = requestScope.runInScope(() -> {
                final Hk2RequestScope.Instance currentInstance = (Hk2RequestScope.Instance) requestScope.suspendCurrent();
                assertTrue(!currentInstance.equals(internalInstance));
                currentInstance.put(inhab, "1");
                return currentInstance;
            });
            assertTrue(!anotherInstance.equals(internalInstance));
            assertEquals("1", anotherInstance.get(inhab));
            anotherInstance.release();
            assertNull(anotherInstance.get(inhab));

            return internalInstance;
        });
        instance.release();
        assertNull(instance.get(inhab));
    }

    @Test
    public void testMultipleGetInstanceCalls() throws Exception {
        final RequestScope requestScope = new Hk2RequestScope();
        assertNull(requestScope.suspendCurrent());
        ForeignDescriptor inhab = ForeignDescriptor.wrap(new TestProvider("a"));
        final Hk2RequestScope.Instance instance = requestScope.runInScope(() -> {
            final Hk2RequestScope.Instance internalInstance = (Hk2RequestScope.Instance) requestScope.suspendCurrent();
            internalInstance.put(inhab, "1");
            requestScope.suspendCurrent();
            requestScope.suspendCurrent();
            requestScope.suspendCurrent();
            requestScope.suspendCurrent();
            return internalInstance;
        });
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertEquals("1", instance.get(inhab));
        instance.release();
        assertNull(instance.get(inhab));
    }

    /**
     * Test request scope inhabitant.
     */
    public static class TestProvider extends AbstractActiveDescriptor<String> {

        private final String id;

        public TestProvider(final String id) {
            super();
            this.id = id;
        }

        @Override
        public Class<?> getImplementationClass() {
            return String.class;
        }

        @Override
        public Type getImplementationType() {
            return getImplementationClass();
        }

        @Override
        public String create(final ServiceHandle<?> root) {
            return id;
        }
    }
}
