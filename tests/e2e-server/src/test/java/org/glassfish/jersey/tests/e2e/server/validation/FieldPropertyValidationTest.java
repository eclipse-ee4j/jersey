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

package org.glassfish.jersey.tests.e2e.server.validation;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class FieldPropertyValidationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(FieldPropertyValidationResource.class);
    }

    @Test
    public void testValid() throws Exception {
        _test("valid", 200);
    }

    @Test
    public void testInvalidPropertyGetterAndClassNull() throws Exception {
        _test("invalidPropertyGetterAndClassNull", 400);
    }

    @Test
    public void testInvalidPropertyGetterAndClassLong() throws Exception {
        _test("invalidPropertyGetterAndClassLong", 400);
    }

    @Test
    public void testInvalidPropertyAndClassNull() throws Exception {
        _test("invalidPropertyAndClassNull", 400);
    }

    @Test
    public void testInvalidFieldAndClassNull() throws Exception {
        _test("invalidFieldAndClassNull", 400);
    }

    @Test
    public void testInvalidPropertyGetterNull() throws Exception {
        _test("invalidPropertyGetterNull", 400);
    }

    @Test
    public void testInvalidPropertyGetterLong() throws Exception {
        _test("invalidPropertyGetterLong", 400);
    }

    @Test
    public void testInvalidPropertyNull() throws Exception {
        _test("invalidPropertyNull", 400);
    }

    @Test
    public void testInvalidFieldNull() throws Exception {
        _test("invalidFieldNull", 400);
    }

    private void _test(final String path, final int status) {
        final Response response = target("fieldPropertyValidationResource")
                .path(path)
                .request()
                .get();

        assertThat(response.getStatus(), equalTo(status));

        if (status == 200) {
            assertThat(response.readEntity(String.class), equalTo("ok"));
        }
    }
}
