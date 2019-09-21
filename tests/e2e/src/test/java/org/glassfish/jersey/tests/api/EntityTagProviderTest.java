/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import javax.ws.rs.core.EntityTag;

import org.glassfish.jersey.message.internal.EntityTagProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Marc Hadley
 */
public class EntityTagProviderTest {

    @Test
    public void testToStringWeak() {
        checkToString(new EntityTag("Hello \"World\"", true), "W/\"Hello \\\"World\\\"\"");
    }

    @Test
    public void testToStringStrong() {
        checkToString(new EntityTag("Hello \"World\""), "\"Hello \\\"World\\\"\"");
    }

    @Test
    public void testFromStringWeak() throws Exception {
        checkFromString("W/\"Hello \\\"World\\\"\"", new EntityTag("Hello \"World\"", true));
    }

    @Test
    public void testFromStringStrong() throws Exception {
        checkFromString("\"Hello \\\"World\\\"\"", new EntityTag("Hello \"World\""));
    }

    private void checkToString(final EntityTag e, final String result) {
        final EntityTagProvider instance = new EntityTagProvider();
        assertEquals(result, instance.toString(e));
    }

    public void checkFromString(final String e, final EntityTag result) throws Exception {
        final EntityTagProvider instance = new EntityTagProvider();
        assertEquals(result, instance.fromString(e));
    }
}
