/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.oauth1;


import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Thomas Meire
 * @author Miroslav Fuksa
 */
public class NonceManagerTest {

    private String stamp() {
        return stamp(0);
    }

    private String stamp(int offset) {
        return Long.toString((System.currentTimeMillis() - offset) / 1000);
    }

    @Test
    public void testExpiredNonce() {
        NonceManager nonces = new NonceManager(1000, 50, TimeUnit.SECONDS, 2000000);

        boolean accepted = nonces.verify("old-nonce-key", stamp(2000), "old-nonce");
        assertFalse(accepted);

        long size = nonces.checkAndGetSize();
        assertEquals(0, size);
    }

    @Test
    public void testValidNonce() {
        NonceManager nonces = new NonceManager(1000, 50, TimeUnit.SECONDS, 2000000);

        boolean accepted = nonces.verify("nonce-key", stamp(), "nonce");
        assertTrue(accepted);

        long size = nonces.checkAndGetSize();
        assertEquals(1, size);
    }

    @Test
    public void testDuplicateNonce() {
        NonceManager nonces = new NonceManager(1000, 50, TimeUnit.SECONDS, 2000000);

        String stamp = stamp();

        boolean accepted;
        accepted = nonces.verify("nonce-key", stamp, "nonce");
        assertTrue(accepted);

        accepted = nonces.verify("nonce-key", stamp, "nonce");
        assertFalse(accepted);
    }

    @Test
    public void testAutoGC() {
        NonceManager nonces = new NonceManager(1000, 10, TimeUnit.SECONDS, 2000000);

        // verify nine
        for (int i = 0; i < 9; i++) {
            assertTrue(nonces.verify("testing-" + i, stamp(), Integer.toString(i)));
        }
        assertEquals(9, nonces.checkAndGetSize());

        // invalid nonces don't trigger gc's
        assertFalse(nonces.verify("testing-9", stamp(2000), "9"));
        assertEquals(9, nonces.checkAndGetSize());

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            fail("Can't guarantee we slept long enough...");
        }
        // 10th valid nonce triggers a gc on old tokens
        assertTrue(nonces.verify("testing-10", stamp(), "10"));
        assertEquals(1, nonces.checkAndGetSize());
    }

    @Test
    public void testManualGC() {
        NonceManager nonces = new NonceManager(1000, 5000, TimeUnit.SECONDS, 2000000);

        // insert 100 valid nonces
        for (int i = 0; i < 100; i++) {
            nonces.verify("testing-" + i, stamp(), Integer.toString(i));
        }
        assertEquals(100, nonces.checkAndGetSize());

        // make sure the gc doesn't clean valid nonces
        nonces.gc(System.currentTimeMillis());
        assertEquals(100, nonces.checkAndGetSize());

        // sleep a while to invalidate the nonces
        try {
            Thread.sleep(1100);
        } catch (Exception e) {
            fail("Can't guarantee we slept long enough...");
        }

        // gc should remove all the nonces
        nonces.gc(System.currentTimeMillis());
        assertEquals(0, nonces.checkAndGetSize());
    }

    @Test
    public void testFutureTimeStamps() {
        NonceManager nonces = new NonceManager(10000, 5000, TimeUnit.SECONDS, 2000000);
        assertFalse(nonces.verify("a", stamp(-20000), "1"));
        assertEquals(0, nonces.checkAndGetSize());
        assertFalse(nonces.verify("a", stamp(-15000), "1"));
        assertEquals(0, nonces.checkAndGetSize());
        assertFalse(nonces.verify("a", stamp(15000), "1"));
        assertEquals(0, nonces.checkAndGetSize());
        final String stamp = stamp(-1000);
        assertTrue(nonces.verify("a", stamp, "1"));
        assertEquals(1, nonces.checkAndGetSize());
        assertFalse(nonces.verify("a", stamp, "1"));
        assertEquals(1, nonces.checkAndGetSize());
        assertTrue(nonces.verify("a", stamp(-2001), "1"));
        assertEquals(2, nonces.checkAndGetSize());
        assertTrue(nonces.verify("a", stamp(-3001), "1"));
        assertEquals(3, nonces.checkAndGetSize());
    }

    @Test
    public void testMaxCacheSize() {
        // initializa max cache size to 3
        NonceManager nonces = new NonceManager(1000, 5000, TimeUnit.MILLISECONDS, 3);
        assertTrue(nonces.verify("a", "1000", "1", 1000));
        assertEquals(1, nonces.checkAndGetSize());
        assertTrue(nonces.verify("a", "1050", "1", 1000));
        assertEquals(2, nonces.checkAndGetSize());
        assertTrue(nonces.verify("a", "1100", "1", 1000));
        assertEquals(3, nonces.checkAndGetSize());

        // this will not fit to the cache (cache is already full)
        assertFalse(nonces.verify("a", "500", "1", 1000));
        assertEquals(3, nonces.checkAndGetSize());

        // now time is 2100, so we clear the cache values lower than 1060
        assertTrue(nonces.verify("a", "2040", "1", 2060));
        assertEquals(2, nonces.checkAndGetSize());
    }

    @Test
    public void testUnits() {
        // initialize max cache size to 3
        NonceManager nonces = new NonceManager(240000, 5000, TimeUnit.MINUTES, 30);
        assertTrue(nonces.verify("a", "1", "1", 60000));
        assertEquals(1, nonces.checkAndGetSize());
        assertFalse(nonces.verify("a", "1", "1", 60001));
        assertEquals(1, nonces.checkAndGetSize());
        assertTrue(nonces.verify("a", "2", "1", 120002));
        assertEquals(2, nonces.checkAndGetSize());

        assertTrue(nonces.verify("a", "3", "1", 180003));
        assertEquals(3, nonces.checkAndGetSize());

        assertFalse(nonces.verify("a", "1", "1", 300000));
        assertEquals(3, nonces.checkAndGetSize());
    }
}
