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

package org.glassfish.jersey.tests.e2e.server.validation.validateonexecution;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.util.runner.RunSeparately;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public abstract class ValidateOnExecutionAbstractTest extends JerseyTest {

    @Test
    public void testOnMethodValidateInputPassValidateExecutableDefault() throws Exception {
        _testOnMethod("validateExecutableDefault", 0, 200);
    }

    @Test
    public void testOnMethodValidateInputFailValidateExecutableDefault() throws Exception {
        _testOnMethod("validateExecutableDefault", 15, 400);
    }

    @Test
    public void testOnMethodValidateInputPassValidateExecutableMatch() throws Exception {
        _testOnMethod("validateExecutableMatch", 0, 200);
    }

    @Test
    public void testOnMethodValidateInputFailValidateExecutableMatch() throws Exception {
        _testOnMethod("validateExecutableMatch", 15, 400);
    }

    @Test
    public void testOnMethodValidateInputPassValidateExecutableMiss() throws Exception {
        _testOnMethod("validateExecutableMiss", 0, 200);
    }

    @Test
    public void testOnMethodValidateInputPassBiggerValidateExecutableMiss() throws Exception {
        _testOnMethod("validateExecutableMiss", 15, 200);
    }

    @Test
    public void testOnMethodValidateInputPassValidateExecutableNone() throws Exception {
        _testOnMethod("validateExecutableNone", 0, 200);
    }

    @Test
    public void testOnMethodValidateInputPassBiggerValidateExecutableNone() throws Exception {
        _testOnMethod("validateExecutableNone", 15, 200);
    }

    @Test
    public void testOnMethodValidateResultPassValidateExecutableDefault() throws Exception {
        _testOnMethod("validateExecutableDefault", 0, 200);
    }

    @Test
    public void testOnMethodValidateResultFailValidateExecutableDefault() throws Exception {
        _testOnMethod("validateExecutableDefault", -15, 500);
    }

    @Test
    public void testOnMethodValidateResultPassValidateExecutableMatch() throws Exception {
        _testOnMethod("validateExecutableMatch", 0, 200);
    }

    @Test
    public void testOnMethodValidateResultFailValidateExecutableMatch() throws Exception {
        _testOnMethod("validateExecutableMatch", -15, 500);
    }

    @Test
    public void testOnMethodValidateResultPassValidateExecutableMiss() throws Exception {
        _testOnMethod("validateExecutableMiss", 0, 200);
    }

    @Test
    public void testOnMethodValidateResultPassBiggerValidateExecutableMiss() throws Exception {
        _testOnMethod("validateExecutableMiss", -15, 200);
    }

    @Test
    public void testOnMethodValidateResultPassValidateExecutableNone() throws Exception {
        _testOnMethod("validateExecutableNone", 0, 200);
    }

    @Test
    public void testOnMethodValidateResultPassBiggerValidateExecutableNone() throws Exception {
        _testOnMethod("validateExecutableNone", -15, 200);
    }

    @Test
    public void testOnTypeValidateInputPassValidateExecutableDefault() throws Exception {
        _testOnType("default", 0, 200);
    }

    @Test
    public void testOnTypeValidateInputPassValidateExecutableMatch() throws Exception {
        _testOnType("match", 0, 200);
    }

    @Test
    public void testOnTypeValidateInputFailValidateExecutableMatch() throws Exception {
        _testOnType("match", 15, 400);
    }

    @Test
    public void testOnTypeValidateInputPassValidateExecutableMiss() throws Exception {
        _testOnType("miss", 0, 200);
    }

    @Test
    public void testOnTypeValidateInputPassBiggerValidateExecutableMiss() throws Exception {
        _testOnType("miss", 15, 200);
    }

    @Test
    public void testOnTypeValidateInputPassValidateExecutableNone() throws Exception {
        _testOnType("none", 0, 200);
    }

    @Test
    public void testOnTypeValidateInputPassBiggerValidateExecutableNone() throws Exception {
        _testOnType("none", 15, 200);
    }

    @Test
    public void testOnTypeValidateResultPassValidateExecutableDefault() throws Exception {
        _testOnType("default", 0, 200);
    }

    @Test
    public void testOnTypeValidateResultPassValidateExecutableMatch() throws Exception {
        _testOnType("match", 0, 200);
    }

    @Test
    @RunSeparately
    public void testOnTypeValidateResultFailValidateExecutableMatch() throws Exception {
        _testOnType("match", -15, 500);
    }

    @Test
    public void testOnTypeValidateResultPassValidateExecutableMiss() throws Exception {
        _testOnType("miss", 0, 200);
    }

    @Test
    @RunSeparately
    public void testOnTypeValidateResultPassBiggerValidateExecutableMiss() throws Exception {
        _testOnType("miss", -15, 200);
    }

    @Test
    public void testOnTypeValidateResultPassValidateExecutableNone() throws Exception {
        _testOnType("none", 0, 200);
    }

    @Test
    @RunSeparately
    public void testOnTypeValidateResultPassBiggerValidateExecutableNone() throws Exception {
        _testOnType("none", -15, 200);
    }

    @Test
    public void testMixedValidatePassDefault() throws Exception {
        _test("mixed-default", 0, 200);
    }

    @Test
    public void testMixedValidateInputFailDefault() throws Exception {
        _test("mixed-default", 15, 400);
    }

    @Test
    public void testMixedValidateResultFailDefault() throws Exception {
        _test("mixed-default", -15, 500);
    }

    @Test
    public void testMixedValidatePassNone() throws Exception {
        _test("mixed-none", 0, 200);
    }

    @Test
    public void testMixedValidateInputPassNone() throws Exception {
        _test("mixed-none", 15, 200);
    }

    @Test
    public void testMixedValidateResultPassNone() throws Exception {
        _test("mixed-none", -15, 200);
    }

    void _testOnMethod(final String path, final Integer value, final int returnStatus) throws Exception {
        _test("on-method/" + path, value, returnStatus);
    }

    void _testOnType(final String path, final Integer value, final int returnStatus) throws Exception {
        _test("on-type-" + path, value, returnStatus);
    }

    void _test(final String path, final Integer value, final int returnStatus) throws Exception {
        final Response response = target(path)
                .request()
                .post(Entity.text(value));

        assertEquals(returnStatus, response.getStatus());

        if (returnStatus == 200) {
            assertEquals(value, response.readEntity(Integer.class));
        }
    }
}
