/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2031;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-2164.
 *
 * @author Michal Gajdos
 */
public class Jersey2031ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Jersey2031();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testTemplateRelative() throws Exception {
        _test("template-relative");
    }

    @Test
    public void testTemplateAbsolute() throws Exception {
        _test("template-absolute");
    }

    @Test
    public void testViewableRelative() throws Exception {
        _test("viewable-relative");
    }

    @Test
    public void testViewableAbsolute() throws Exception {
        _test("viewable-absolute");
    }

    private void _test(final String path) throws Exception {
        final Response response = target(path).request("text/html").get();
        final String page = response.readEntity(String.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(page, containsString("index"));
        assertThat(page, containsString("include"));
    }
}
