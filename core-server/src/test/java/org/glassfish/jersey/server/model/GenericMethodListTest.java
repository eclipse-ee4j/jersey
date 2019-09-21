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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;

import javax.ws.rs.POST;
import javax.ws.rs.core.Context;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Paul Sandoz
 */
public class GenericMethodListTest {

    public abstract static class AFoo<T, V> {

        @POST
        public abstract T create(T newObject, @Context String s, @Context V v);
    }

    public abstract static class ABar extends AFoo<String, Integer> {
    }

    public class AResource extends ABar {

        public String create(String newObject, String s, Integer v) {
            return newObject;
        }
    }

    @Test
    public void testGenericAbstractClasses() {
        MethodList ml = new MethodList(AResource.class);
        Iterator<AnnotatedMethod> i = ml.iterator();
        assertTrue(i.hasNext());

        AnnotatedMethod am = i.next();
        Method m = am.getMethod();
        Type[] types = m.getGenericParameterTypes();
        assertTrue(types[0] instanceof TypeVariable);
        assertTrue(types[1] instanceof Class);
        assertTrue(types[2] instanceof TypeVariable);
    }

    public static interface IFoo<T, V> {

        @POST
        public T create(T newObject, @Context String s, @Context V v);
    }

    public static interface IBar extends IFoo<String, Integer> {
    }

    public class IResource implements IBar {

        public String create(String newObject, String s, Integer v) {
            return newObject;
        }
    }

    @Test
    public void testGeneriInterfaceClasses() {
        MethodList ml = new MethodList(IResource.class);
        Iterator<AnnotatedMethod> i = ml.iterator();
        assertTrue(i.hasNext());

        AnnotatedMethod am = i.next();
        Method m = am.getMethod();
        Type[] types = m.getGenericParameterTypes();
        assertTrue(types[0] instanceof TypeVariable);
        assertTrue(types[1] instanceof Class);
        assertTrue(types[2] instanceof TypeVariable);
    }
}
