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

package org.glassfish.jersey.tests.integration.j441.two;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This is to make sure two Jersey wars are separated well in a single ear.
 *
 * @author Michal Gajdos
 */
public class ContextPathTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testFieldInjection() {
        assertThat(target("two/test/field").request().get(String.class), is("/two"));
    }

    @Test
    public void testConstructorInjection() {
        assertThat(target("two/test/ctor-param").request().get(String.class), is("/two"));
    }

    @Test
    public void testMethodInjection() {
        assertThat(target("two/test/method-param").request().get(String.class), is("/two"));
    }

    @Test
    public void testExceptionMapperInjection() {
        assertThat(target("two/test/exception").request().get(String.class), is("/two"));
    }
}
