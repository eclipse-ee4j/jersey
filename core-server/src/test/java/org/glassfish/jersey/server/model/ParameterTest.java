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

package org.glassfish.jersey.server.model;


import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Parameter model creation test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ParameterTest {

    private static interface GenericContractInterface<T> {
        public abstract T process(T param);
    }

    private abstract static class GenericContractClass<T> {
        public abstract T process(T param);
    }

    private static interface ContractInterface {
        public String process(String param);
    }

    private abstract static class ContractClass {
        public abstract String process(String param);
    }

    private static class StandaloneServiceClass {
        public String process(String param) {
            return null;
        }
    }

    private static class GenericCIService implements GenericContractInterface<String> {

        @Override
        public String process(String param) {
            return null;
        }
    }

    private static class GenericCIGenericService implements GenericContractInterface<List<String>> {

        @Override
        public List<String> process(List<String> param) {
            return null;
        }
    }

    private static class GenericCCService extends GenericContractClass<String> {

        @Override
        public String process(String param) {
            return null;
        }
    }

    private static class GenericCCGenericService extends GenericContractClass<List<String>> {

        @Override
        public List<String> process(List<String> param) {
            return null;
        }
    }

    private static class CIService implements ContractInterface {

        @Override
        public String process(String param) {
            return null;
        }
    }

    private static class CCService extends ContractClass {

        @Override
        public String process(String param) {
            return null;
        }
    }


    private static class GenericCCGenericArrayService extends GenericContractClass<byte[]> {

        @Override
        public byte[] process(byte[] param) {
            return null;
        }
    }

    private static class GenericCIGenericArrayService implements GenericContractInterface<byte[]> {

        @Override
        public byte[] process(byte[] param) {
            return null;
        }
    }

    public void testParameterCreation() throws NoSuchMethodException {
        Class<?> implementing = StandaloneServiceClass.class;
        Class<?> declaring = StandaloneServiceClass.class;
        Method method = declaring.getMethod("process", Object.class);
        final List<Parameter> parameters = Parameter.create(implementing, declaring, method, false);
        assertEquals(1, parameters.size());
        Parameter parameter = parameters.get(0);
        assertEquals(String.class, parameter.getRawType());
        assertEquals(String.class, parameter.getType());
    }

    /**
     * JERSEY-2408 Fix test - missing hashCode() and equals() in {@link Parameter} caused
     * the descriptorCache in {@link org.glassfish.jersey.server.internal.inject.DelegatedInjectionValueFactoryProvider} not to
     * reuse the Parameter instances (Parameter was used as a key in a {@link org.glassfish.hk2.utilities.cache.Cache},
     * which delegates to an underlying {@link java.util.concurrent.ConcurrentHashMap}.
     */
    @Test
    public void testParameterHashCode() {
        Annotation[] annotations = new Annotation[]{new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Inject.class;
            }
        }};
        Parameter param1 = Parameter.create(String.class, String.class, false, String.class, String.class, annotations);
        Parameter param2 = Parameter.create(String.class, String.class, false, String.class, String.class, annotations);
        Parameter param3 = Parameter.create(Integer.class, Integer.class, false, Integer.class, Integer.class, annotations);

        assertEquals(param1, param2);
        assertEquals(param1.hashCode(), param2.hashCode());
        assertNotEquals(param1, param3);
        assertNotEquals(param1.hashCode(), param3.hashCode());
    }

}
