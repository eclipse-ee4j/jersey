/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test whether cases described in {@code README} are passing.
 *
 * @author Michal Gajdos
 */
public class MainTest {

    @Test
    public void testSync() throws Exception {
        assertEquals(0, Main.runClient(new String[]{"req=10", "mode=sync"}));
    }

    @Test
    public void testAsync() throws Exception {
        assertEquals(0, Main.runClient(new String[]{"req=10", "mode=async"}));
    }

    @Test
    public void testAsyncWrongUri() throws Exception {
        assertEquals(-1, Main.runClient(new String[]{"req=1", "mode=async", "uri=http://foo.bar"}));
    }

    @Test
    public void testSyncWrongUri() throws Exception {
        assertEquals(-1, Main.runClient(new String[]{"req=1", "mode=sync", "uri=http://foo.bar"}));
    }
}
