/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashSet;
import java.util.Set;

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

    private void test(Class c) {
        test(c, false);
    }

    private void test(Class c, boolean privateMethods) {
        MethodList ml = new MethodList(CPublic.class, privateMethods);

        Set<String> s = new HashSet<String>();
        for (AnnotatedMethod am : ml) {
            s.add(am.getMethod().getName());
        }

        assertTrue(s.contains("a"));
        assertTrue(s.contains("b"));
        assertTrue(s.contains("c"));
    }
}
