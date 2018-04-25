/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.internal.inject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.GenericType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import org.junit.Test;
import static org.junit.Assert.assertSame;

/**
 * Referencing factory test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ReferencingFactoryTest extends AbstractBinder {

    private static class Foo {
        final int value;

        private Foo(int value) {
            this.value = value;
        }
    }

    private static class ValueInjected {

        @Inject
        Foo foo;
        @Inject
        List<Integer> integers;
        @Inject
        List<String> strings;
    }

    private static class RefInjected {

        @Inject
        Ref<Foo> foo;
        @Inject
        Ref<List<Integer>> integers;
        @Inject
        Ref<List<String>> strings;
    }

    //
    private Foo expectedFoo = null;
    private List<Integer> expectedIntegers = null;
    private List<String> expectedStrings = new LinkedList<String>();

    private static final class FooReferencingFactory extends ReferencingFactory<Foo> {
        @Inject
        public FooReferencingFactory(Provider<Ref<Foo>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static final class ListOfIntegerReferencingFactory extends ReferencingFactory<List<Integer>> {
        @Inject
        public ListOfIntegerReferencingFactory(Provider<Ref<List<Integer>>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static final class ListOfStringReferencingFactory extends ReferencingFactory<List<String>> {
        @Inject
        public ListOfStringReferencingFactory(Provider<Ref<List<String>>> referenceFactory) {
            super(referenceFactory);
        }
    }

    @Override
    protected void configure() {
        bindFactory(FooReferencingFactory.class).to(Foo.class);
        bindFactory(ReferencingFactory.referenceFactory()).to(new GenericType<Ref<Foo>>() {}).in(Singleton.class);

        bindFactory(ListOfIntegerReferencingFactory.class).to(new GenericType<List<Integer>>() {});
        bindFactory(ReferencingFactory.referenceFactory()).to(new GenericType<Ref<List<Integer>>>() {
        }).in(Singleton.class);

        bindFactory(ListOfStringReferencingFactory.class).to(new GenericType<List<String>>() {});
        bindFactory(ReferencingFactory.referenceFactory(expectedStrings)).to(new GenericType<Ref<List<String>>>() {
        }).in(Singleton.class);
    }

    /**
     * Referenced binding test.
     */
    @Test
    public void testReferencedBinding() {
        InjectionManager injectionManager = Injections.createInjectionManager(this);
        injectionManager.completeRegistration();

        RefInjected refValues = injectionManager.createAndInitialize(RefInjected.class);
        expectedFoo = new Foo(10);
        refValues.foo.set(expectedFoo);
        expectedIntegers = new LinkedList<Integer>();
        refValues.integers.set(expectedIntegers);
        expectedStrings = new ArrayList<String>();
        refValues.strings.set(expectedStrings);

        ValueInjected updatedValues = injectionManager.createAndInitialize(ValueInjected.class);
        assertSame(expectedFoo, updatedValues.foo);
        assertSame(expectedIntegers, updatedValues.integers);
        assertSame(expectedStrings, updatedValues.strings);
    }
}
