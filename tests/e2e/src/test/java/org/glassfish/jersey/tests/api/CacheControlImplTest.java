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

import javax.ws.rs.core.CacheControl;

import org.glassfish.jersey.message.internal.CacheControlProvider;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Marc Hadley
 */
public class CacheControlImplTest {

    @Test
    public void testToString() {
        CacheControlProvider p = new CacheControlProvider();
        CacheControl instance = new CacheControl();

        instance.setNoCache(true);
        String expResult = "no-cache, no-transform";
        String result = p.toString(instance);
        assertEquals(expResult, result);

        instance.setNoStore(true);
        expResult = "no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.setPrivate(true);
        expResult = "private, no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.getPrivateFields().add("Fred");
        expResult = "private=\"Fred\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getPrivateFields().add("Bob");
        expResult = "private=\"Fred, Bob\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance = new CacheControl();
        instance.getCacheExtension().put("key1", "value1");
        expResult = "no-transform, key1=value1";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getCacheExtension().put("key1", "value1 with spaces");
        expResult = "no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.setNoStore(true);
        expResult = "no-store, no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance = new CacheControl();
        instance.getCacheExtension().put("key1", null);
        expResult = "no-transform, key1";
        result = p.toString(instance);
        assertEquals(expResult, result);
    }

    @Test
    public void testRoundTrip() {
        checkRoundTrip("no-cache, no-transform");
        checkRoundTrip("no-cache, no-store, no-transform");
        checkRoundTrip("private, no-cache, no-store, no-transform");
        checkRoundTrip("private=\"Fred\", no-cache, no-store, no-transform");
        checkRoundTrip("private=\"Fred, Bob\", no-cache, no-store, no-transform");
        checkRoundTrip("no-transform, key1=value1");
        checkRoundTrip("no-transform, key1=\"value1 with spaces\"");
        checkRoundTrip("no-store, no-transform, key1=\"value1 with spaces\"");
        checkRoundTrip("no-transform, key1");
        checkRoundTrip("must-revalidate, proxy-revalidate");
        checkRoundTrip("max-age=1, s-maxage=1");
    }

    private void checkRoundTrip(String s) {
        CacheControlProvider p = new CacheControlProvider();

        CacheControl cc1 = p.fromString(s);
        CacheControl cc2 = p.fromString(cc1.toString());
        cc2.toString();

        cc1.equals(cc2);

        try {
            assertEquals(cc1, cc2);
        } catch (RuntimeException ex) {
            throw ex;
        }
    }
}
