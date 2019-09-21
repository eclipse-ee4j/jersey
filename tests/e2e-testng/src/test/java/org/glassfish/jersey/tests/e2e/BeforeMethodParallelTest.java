/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e;

import org.glassfish.jersey.test.ContainerPerMethodTestNgStrategy;
import org.glassfish.jersey.test.spi.TestNgStrategy;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Runs a set of tests (in parallel) each against separate test container instance.
 *
 * @author Michal Gajdos
 */
public class BeforeMethodParallelTest extends AbstractParallelTest {

    @BeforeMethod
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterMethod
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected TestNgStrategy configureStrategy() {
        return new ContainerPerMethodTestNgStrategy();
    }

    @Override
    protected void testValue(final Integer actual) {
        assertThat(actual, is(0));
    }
}
