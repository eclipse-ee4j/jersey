/*
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

import org.junit.Assert;
import org.junit.Test;

import static org.glassfish.jersey.microprofile.restclient.RestClientBuilderImpl.createProxyString;

public class RestClientBuilderImplTest {

    @Test
    public void createProxyStringTest() {
        Assert.assertTrue(createProxyString("localhost", 8765).equals("http://localhost:8765"));
        Assert.assertTrue(createProxyString("http://localhost", 8765).equals("http://localhost:8765"));
        Assert.assertTrue(createProxyString("127.0.0.1", 8765).equals("http://127.0.0.1:8765"));
        Assert.assertTrue(createProxyString("http://192.168.1.1", 8765).equals("http://192.168.1.1:8765"));
    }
}
