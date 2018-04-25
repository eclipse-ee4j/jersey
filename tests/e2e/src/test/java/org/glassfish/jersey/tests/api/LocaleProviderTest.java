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

import java.util.Locale;

import org.glassfish.jersey.message.internal.LocaleProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Hadley
 */
public class LocaleProviderTest {
    @Test
    public void testToString() {
        final LocaleProvider instance = new LocaleProvider();
        assertEquals("en", instance.toString(new Locale("en")));
        assertEquals("en-US", instance.toString(new Locale("en", "us")));
    }

    @Test
    public void testFromString() throws Exception {
        final LocaleProvider instance = new LocaleProvider();
        assertEquals(new Locale("en"), instance.fromString("en"));
        assertEquals(new Locale("en", "us"), instance.fromString("en-us"));
    }

}
