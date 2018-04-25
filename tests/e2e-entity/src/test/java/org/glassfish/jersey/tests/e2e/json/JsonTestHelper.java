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

package org.glassfish.jersey.tests.e2e.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
public class JsonTestHelper {

    public static String getResourceAsString(String prefix, String resource) throws IOException {
        return getEntityAsString(Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix + resource));
    }

    public static String getEntityAsString(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }

    public static <T> T createTestInstance(Class<T> clazz) {
        try {
            Method createMethod = clazz.getDeclaredMethod("createTestInstance");
            //noinspection unchecked
            return (T) createMethod.invoke(clazz);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isCollectionEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean areCollectionsEqual(final Collection<T> collection1, final Collection<T> collection2) {
        return collection1 == collection2
                || (isCollectionEmpty(collection1) && isCollectionEmpty(collection2))
                || (collection1 != null && collection1.equals(collection2));
    }

    public static boolean isArrayEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean areArraysEqual(final T[] array1, final T[] array2) {
        return array1 == array2
                || (isArrayEmpty(array1) && isArrayEmpty(array2))
                || Arrays.equals(array1, array2);
    }

}
