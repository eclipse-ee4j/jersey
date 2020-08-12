/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.util.Arrays;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class InterfaceUtilTest {

    @Test
    public void testGetAllParams() {
        assertEquals(InterfaceUtil.getAllMatchingParams(
            "{abc}/{xyzId: [0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}"),
            Arrays.asList("abc", "xyzId"));
        assertEquals(InterfaceUtil.getAllMatchingParams(
            "{xyzId: [a-zA-Z]+}/{abc}"), Arrays.asList("xyzId", "abc"));
    }
}
