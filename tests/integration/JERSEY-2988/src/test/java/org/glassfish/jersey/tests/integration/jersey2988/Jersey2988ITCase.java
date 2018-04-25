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

package org.glassfish.jersey.tests.integration.jersey2988;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * JERSEY-2988 reproducer and JERSEY-2990 (duplicate of the previous one)
 *
 * @author Petr Bouda
 */
public class Jersey2988ITCase extends JerseyTest {

    private static final String HEADER_NAME = "x-test-header";
    private static final String HEADER_VALUE = "cool-header";

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Reproducer for JERSEY-2988
     *
     * @throws Exception
     */
    @Test
    public void contextFieldInjection() throws Exception {
        testCdiBeanContextInjection("field");
    }

    @Test
    public void contextSetterInjection() throws Exception {
        testCdiBeanContextInjection("setter");
    }

    private void testCdiBeanContextInjection(String path) {
        int status = target("test/" + path).request().get().getStatus();
        if (status != 200) {
            fail("@Context field is not properly injected into CDI Bean.");
        }
    }

    /**
     * Reproducer for JERSEY-2990
     *
     * @throws Exception
     */
    @Test
    public void contextFieldInjectionExceptionMapper() throws Exception {
        testExceptionMapperContextInjection("field");
    }

    @Test
    public void contextSetterExceptionMapper() throws Exception {
        testExceptionMapperContextInjection("setter");
    }

    private void testExceptionMapperContextInjection(String path) {
        Response response = target("test/ex/" + path).request().header(HEADER_NAME, HEADER_VALUE).get();
        if (response.getStatus() != 520 || !HEADER_VALUE.equals(response.getHeaderString(HEADER_NAME))) {
            fail("@Context method was not properly injected into ExceptionMapper.");
        }
    }
}
