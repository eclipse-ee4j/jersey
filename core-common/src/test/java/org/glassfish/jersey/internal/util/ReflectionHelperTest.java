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

package org.glassfish.jersey.internal.util;

import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * {@code ReflectionHelper} unit tests.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("unchecked")
public class ReflectionHelperTest {

    @SuppressWarnings("UnusedDeclaration")
    public static interface I<T> {
    }

    public static class A<T> implements I<T> {
    }

    public static class TestNoInterface extends A<byte[]> {
    }

    public static class TestInterface extends A<byte[]> implements I<byte[]> {
    }

    /**
     * See JERSEY-1598.
     */
    @Test
    public void getParameterizedClassArgumentsTest() {
        ReflectionHelper.DeclaringClassInterfacePair dcip = ReflectionHelper.getClass(TestNoInterface.class, I.class);
        Class[] arguments = ReflectionHelper.getParameterizedClassArguments(dcip);
        final Class aClass = arguments[0];

        dcip = ReflectionHelper.getClass(TestInterface.class, I.class);
        arguments = ReflectionHelper.getParameterizedClassArguments(dcip);
        assertEquals(aClass, arguments[0]);
    }

    @Test(expected = AccessControlException.class)
    public void securityManagerSetContextClassLoader() throws Exception {
        final ClassLoader loader = ReflectionHelper.class.getClassLoader();

        Thread.currentThread().setContextClassLoader(loader);
        fail("It should not be possible to set context class loader from unprivileged block");
    }

    @Test(expected = AccessControlException.class)
    public void securityManagerSetContextClassLoaderPA() throws Exception {
        final ClassLoader loader = ReflectionHelper.class.getClassLoader();

        ReflectionHelper.setContextClassLoaderPA(loader).run();
        fail("It should not be possible to set context class loader from unprivileged block even via Jersey ReflectionHelper");
    }

    @Test(expected = AccessControlException.class)
    public void securityManagerSetContextClassLoaderInDoPrivileged() throws Exception {
        final ClassLoader loader = ReflectionHelper.class.getClassLoader();

        AccessController.doPrivileged(ReflectionHelper.setContextClassLoaderPA(loader));
        fail("It should not be possible to set context class loader even from privileged block via Jersey ReflectionHelper "
                + "utility");
    }

    public static class FromStringClass {

        private final String value;

        public FromStringClass(final String value) {
            this.value = value;
        }

        public static FromStringClass valueOf(final String value) {
            return new FromStringClass(value);
        }

        public static FromStringClass fromString(final String value) {
            return new FromStringClass(value);
        }
    }

    public static class InvalidFromStringClass {

        private final String value;

        public InvalidFromStringClass(final String value) {
            this.value = value;
        }

        public static Boolean valueOf(final String value) {
            throw new AssertionError("Should not be invoked");
        }

        public static Boolean fromString(final String value) {
            throw new AssertionError("Should not be invoked");
        }
    }

    /**
     * Reproducer for JERSEY-2801.
     */
    @Test
    public void testGetValueOfStringMethod() throws Exception {
        final PrivilegedAction<Method> methodPA = ReflectionHelper.getValueOfStringMethodPA(FromStringClass.class);
        final FromStringClass value = (FromStringClass) methodPA.run().invoke(null, "value");

        assertThat("Incorrect instance of FromStringClass created.", value.value, is("value"));
    }

    /**
     * Negative reproducer for JERSEY-2801.
     */
    @Test
    public void testGetValueOfStringMethodNegative() throws Exception {
        final PrivilegedAction<Method> methodPA = ReflectionHelper.getValueOfStringMethodPA(InvalidFromStringClass.class);

        assertThat("Invalid valueOf method found.", methodPA.run(), nullValue());
    }

    /**
     * Reproducer for JERSEY-2801.
     */
    @Test
    public void testGetFromStringStringMethod() throws Exception {
        final PrivilegedAction<Method> methodPA = ReflectionHelper.getFromStringStringMethodPA(FromStringClass.class);
        final FromStringClass value = (FromStringClass) methodPA.run().invoke(null, "value");

        assertThat("Incorrect instance of FromStringClass created.", value.value, is("value"));
    }

    /**
     * Negative reproducer for JERSEY-2801.
     */
    @Test
    public void testGetFromStringStringMethodNegative() throws Exception {
        final PrivilegedAction<Method> methodPA = ReflectionHelper.getFromStringStringMethodPA(InvalidFromStringClass.class);

        assertThat("Invalid valueOf method found.", methodPA.run(), nullValue());
    }

    public static class IsGetterTester {

        public int get() {
            return 0;
        }

        public boolean is() {
            return true;
        }

        public int getSomething() {
            return 0;
        }

        public boolean isSomething() {
            return true;
        }
    }

    @Test
    public void testIsGetterWithGetOnlyNegative() throws Exception {
        assertThat("isGetter should have returned false for method named 'get'",
                ReflectionHelper.isGetter(IsGetterTester.class.getMethod("get")), is(false));
    }

    @Test
    public void testIsGetterWithIsOnlyNegative() throws Exception {
        assertThat("isGetter should have returned false for method named 'is'",
                ReflectionHelper.isGetter(IsGetterTester.class.getMethod("is")), is(false));
    }

    @Test
    public void testIsGetterWithRealGetterPositive() throws Exception {
        assertThat("isGetter should have returned true for method named 'getSomething'",
                ReflectionHelper.isGetter(IsGetterTester.class.getMethod("getSomething")), is(true));
    }

    @Test
    public void testIsGetterWithRealIsPositive() throws Exception {
        assertThat("isGetter should have returned true for method named 'isSomething'",
                ReflectionHelper.isGetter(IsGetterTester.class.getMethod("isSomething")), is(true));
    }
}
