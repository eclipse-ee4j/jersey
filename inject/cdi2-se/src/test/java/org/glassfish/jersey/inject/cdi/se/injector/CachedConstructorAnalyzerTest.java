/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import javax.enterprise.inject.InjectionException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link CachedConstructorAnalyzer}.
 */
public class CachedConstructorAnalyzerTest {

    private static final Collection<Class<? extends Annotation>> ANNOTATIONS =
            Arrays.asList(Context.class, PathParam.class);

    @Test
    public void testDefaultConstructor() {
        CachedConstructorAnalyzer<DefaultConstructor> analyzer =
                new CachedConstructorAnalyzer<>(DefaultConstructor.class, ANNOTATIONS);

        assertEquals(0, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testNoArgsConstructor() {
        CachedConstructorAnalyzer<NoArgsConstructor> analyzer =
                new CachedConstructorAnalyzer<>(NoArgsConstructor.class, ANNOTATIONS);

        assertEquals(0, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testSingleAnnotatedConstructor() {
        CachedConstructorAnalyzer<SingleAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(SingleAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(1, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testSingleMultiAnnotatedConstructor() {
        CachedConstructorAnalyzer<SingleMultiAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(SingleMultiAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(2, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testLargestAnnotatedConstructor() {
        CachedConstructorAnalyzer<LargestAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(LargestAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(3, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testContainsSmallerNonAnnotatedConstructor() {
        CachedConstructorAnalyzer<ContainsSmallerNonAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(ContainsSmallerNonAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(2, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testContainsLargerNonAnnotatedConstructor() {
        CachedConstructorAnalyzer<ContainsLargerNonAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(ContainsLargerNonAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(1, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testSameNonAnnotatedConstructor() {
        CachedConstructorAnalyzer<SameNonAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(SameNonAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(1, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testBothAnnotatedConstructor() {
        CachedConstructorAnalyzer<BothAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(BothAnnotatedConstructor.class, ANNOTATIONS);

        Constructor<BothAnnotatedConstructor> constructor = analyzer.getConstructor();
        assertEquals(1, constructor.getParameterCount());
        assertEquals(Integer.class, constructor.getParameterTypes()[0]);
    }

    @Test
    public void testOneNonAnnotatedConstructor() {
        CachedConstructorAnalyzer<OneNonAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(OneNonAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(1, analyzer.getConstructor().getParameterCount());
    }

    @Test
    public void testMultiAnnotatedConstructor() {
        CachedConstructorAnalyzer<MultiAnnotatedConstructor> analyzer =
                new CachedConstructorAnalyzer<>(MultiAnnotatedConstructor.class, ANNOTATIONS);

        assertEquals(2, analyzer.getConstructor().getParameterCount());
    }

    @Test(expected = InjectionException.class)
    public void testUnknownAnnotatedConstructor() {
        new CachedConstructorAnalyzer<>(UnknownAnnotatedConstructor.class, ANNOTATIONS).getConstructor();
    }

    @Test(expected = InjectionException.class)
    public void testSingleNonAnnotatedConstructor() {
        new CachedConstructorAnalyzer<>(SingleNonAnnotatedConstructor.class, ANNOTATIONS).getConstructor();
    }

    public static class DefaultConstructor {
    }

    public static class NoArgsConstructor {
        public NoArgsConstructor() {
        }
    }

    public static class SingleNonAnnotatedConstructor {
        public SingleNonAnnotatedConstructor(String str) {
        }
    }

    public static class SingleAnnotatedConstructor {
        public SingleAnnotatedConstructor(@Context String str) {
        }
    }

    public static class SingleMultiAnnotatedConstructor {
        public SingleMultiAnnotatedConstructor(@Context String str, @PathParam("name") String name) {
        }
    }

    public static class LargestAnnotatedConstructor {
        public LargestAnnotatedConstructor(@Context String str, @PathParam("name") String name, @Context String str2) {
        }

        public LargestAnnotatedConstructor(@Context String str) {
        }

        public LargestAnnotatedConstructor(@Context String str, @PathParam("name") String name) {
        }
    }

    public static class ContainsSmallerNonAnnotatedConstructor {
        public ContainsSmallerNonAnnotatedConstructor(String str) {
        }

        public ContainsSmallerNonAnnotatedConstructor(@Context String str, @PathParam("name") String name) {
        }
    }

    public static class ContainsLargerNonAnnotatedConstructor {
        public ContainsLargerNonAnnotatedConstructor(@Context String str) {
        }

        public ContainsLargerNonAnnotatedConstructor(String str, String name) {
        }
    }

    public static class SameNonAnnotatedConstructor {
        public SameNonAnnotatedConstructor(@Context String str) {
        }

        public SameNonAnnotatedConstructor(Integer name) {
        }
    }

    public static class BothAnnotatedConstructor {
        public BothAnnotatedConstructor(@Context String str) {
        }

        public BothAnnotatedConstructor(@Context Integer name) {
        }
    }

    public static class OneNonAnnotatedConstructor {
        public OneNonAnnotatedConstructor(@Context String str) {
        }

        public OneNonAnnotatedConstructor(@Context Integer name, String str) {
        }
    }

    public static class MultiAnnotatedConstructor {
        public MultiAnnotatedConstructor(@Context Integer name, @PathParam("str") @Context String str) {
        }
    }

    public static class UnknownAnnotatedConstructor {
        public UnknownAnnotatedConstructor(@Context Integer name, @MatrixParam("matrix") String str) {
        }
    }
}
