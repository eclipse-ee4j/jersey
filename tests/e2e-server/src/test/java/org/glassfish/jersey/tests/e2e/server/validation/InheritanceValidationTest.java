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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class InheritanceValidationTest extends JerseyTest {

    public static interface ResourceNumberInterface<T extends Number> {

        @POST
        @Min(0)
        @NotNull
        public T post(@NotNull @Max(100) final T value);
    }

    public static interface ResourceStringInterface {

        @Min(-50)
        public String post(@Max(50) final String value);
    }

    @Path("/")
    public static class ResourceNumberString implements ResourceNumberInterface<Integer>, ResourceStringInterface {

        @Override
        public Integer post(final Integer value) {
            return value;
        }

        @POST
        @Path("string")
        @Override
        public String post(final String value) {
            return value;
        }
    }

    @Path("/sub")
    public static class SubClassResourceNumberString extends ResourceNumberString {

        @Override
        public Integer post(final Integer value) {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ResourceNumberString.class, SubClassResourceNumberString.class);
    }

    @Test
    public void testValidateNumberPositive() throws Exception {
        _test(75, 200);
    }

    @Test
    public void testValidateNumberInputNegative() throws Exception {
        _test(150, 400);
    }

    @Test
    public void testValidateStringPositive() throws Exception {
        _test("string", "25", 200);
    }

    @Test
    public void testValidateStringInputNegative() throws Exception {
        _test("string", "150", 400);
    }

    @Test
    public void testValidateNumberSubClassPositive() throws Exception {
        _test("sub", 75, 200);
    }

    @Test
    public void testValidateNumberInputSubClassNegative() throws Exception {
        _test("sub", 150, 400);
    }

    @Test
    public void testValidateStringSubClassPositive() throws Exception {
        _test("sub/string", "25", 200);
    }

    @Test
    public void testValidateStringInputSubClassNegative() throws Exception {
        _test("sub/string", "150", 400);
    }

    @Test
    public void testValidateNumberResponseNegative() throws Exception {
        _test(-150, 500);
    }

    @Test
    public void testValidateStringResponseNegative() throws Exception {
        _test("string", "-150", 500);
    }

    @Test
    public void testValidateNumberResponseSubClassNegative() throws Exception {
        _test("sub", -150, 500);
    }

    @Test
    public void testValidateStringResponseSubClassNegative() throws Exception {
        _test("sub/string", "-150", 500);
    }

    private void _test(final Object value, final int responseStatus) {
        _test("", value, responseStatus);
    }

    private void _test(final String path, final Object value, final int responseStatus) {
        final Response response = target(path).request().post(Entity.text(value));

        assertThat("Wrong response.", response.getStatus(), equalTo(responseStatus));

        if (responseStatus == 200) {
            assertThat("Invalid entity.", response.readEntity(value.getClass()), equalTo(value));
        }
    }
}
