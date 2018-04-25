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

package org.glassfish.jersey.server.internal;

import org.junit.Test;
import static org.glassfish.jersey.server.internal.ContainerUtils.getHandlerPath;
import static org.glassfish.jersey.server.internal.ContainerUtils.reduceLeadingSlashes;
import static org.junit.Assert.assertNull;

import static junit.framework.TestCase.assertEquals;

/**
 * Tests container-oriented utilities functions.
 *
 * @author Petr Bouda
 */
public class ContainerUtilsTest {

    @Test
    public void testReduceLeadingSlashes() {
        assertNull(reduceLeadingSlashes(null));
        assertEquals("path/to/endpoint", reduceLeadingSlashes("path/to/endpoint"));
        assertEquals("/path/to/endpoint", reduceLeadingSlashes("/path/to/endpoint"));
        assertEquals("/path/to/endpoint", reduceLeadingSlashes("////path/to/endpoint"));
        assertEquals("/path//", reduceLeadingSlashes("////path//"));
    }

    @Test
    public void testGetHandlerPath() {
        assertNull(getHandlerPath(null));
        assertEquals("", getHandlerPath(""));
        assertEquals("/path/to/endpoint", getHandlerPath("/path/to/endpoint"));
        assertEquals("////path/to/endpoint", getHandlerPath("////path/to/endpoint"));
        assertEquals("/path/to/endpoint", getHandlerPath("/path/to/endpoint?"));
        assertEquals("/path/to/endpoint", getHandlerPath("/path/to/endpoint?bar=123&baz=435"));
    }
}
