/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import org.junit.Test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

/**
 * @author Paul Sandoz
 */
public class MethodListTest {
    public class CPublic {
        public void a() {}
        public void b() {}
        public void c() {}
    }

    public interface IPublic {
        public void a();
        public void b();
        public void c();
    }

    public class CPrivate {
        private void a() {}
        private void b() {}
        private void c() {}
    }

    public class CPrivateBase {
        private void a() {}
    }

    public class CPrivateInherited extends CPrivateBase {
        private void b() {}
        private void c() {}
    }

    public class CSynthetic {
        class CWithField {
            private int x;
        }

        public int a() {
            return new CWithField().x;
        }

        public void b(int x) {
            new CWithField().x = x;
        }
    }

    public class CBridgeClass implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }

    @Test
    public void testClassPublicMethods() {
        test(CPublic.class);
    }

    @Test
    public void testInterfacePublicMethods() {
        test(IPublic.class);
    }

    @Test
    public void testClassPrivateMethodsInherited() {
        test(CPrivateInherited.class, true);
    }

    @Test
    public void testClassPrivateMethods() {
        test(CPrivate.class, true);
    }

    @Test
    public void testSyntheticMethods() {
        assertTrue(CSynthetic.CWithField.class.getDeclaredMethods().length == 2);

        MethodList ml = new MethodList(CSynthetic.CWithField.class, true);
        assertTrue(!ml.iterator().hasNext());
    }

    @Test
    public void testBridgeMethods() {
        assertTrue(CBridgeClass.class.getDeclaredMethods().length ==  3);

        MethodList ml = new MethodList(CBridgeClass.class, true);
        AtomicInteger count = new AtomicInteger(0);
        ml.forEach(x -> count.addAndGet(1));
        assertTrue(count.get() == 2);
    }

    private void test(Class c) {
        test(c, false);
    }

    private void test(Class c, boolean privateMethods) {
        MethodList ml = new MethodList(c, privateMethods);

        Set<String> s = new HashSet<String>();
        for (AnnotatedMethod am : ml) {
            s.add(am.getMethod().getName());
        }

        assertTrue(s.contains("a"));
        assertTrue(s.contains("b"));
        assertTrue(s.contains("c"));
    }
}
