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

package org.glassfish.jersey.test.util.runner;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * Test concurrent runner invokes all test methods.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(ConcurrentRunner.class)
public class ConcurrentRunnerTest extends TestCase {

    final AtomicInteger test1Invocations = new AtomicInteger();
    final AtomicInteger test2Invocations = new AtomicInteger();
    final AtomicInteger beforeInvocations = new AtomicInteger();

    @Before
    public void before() {
        beforeInvocations.incrementAndGet();
    }

    @Test
    public void testOne() {
        _test(test1Invocations);
    }

    @Test
    public void testTwo() {
        _test(test2Invocations);
    }

    @Test
    @Ignore
    public void ignoredTest() {
        fail("The test should be ignored!");
    }

    private void _test(AtomicInteger testCounter) {
        if (testCounter.getAndIncrement() > 0) {
            fail("test method invoked more than once!");
        }
        if (beforeInvocations.get() == 0) {
            fail("Before test method has not been called!");
        }
    }

}
