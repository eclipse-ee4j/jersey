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

package org.glassfish.jersey.server.internal.monitoring;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Response statistics tests.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class ResponseStatisticsImplTest {

    @Test
    public void testCaching() {
        ResponseStatisticsImpl.Builder builder = new ResponseStatisticsImpl.Builder();

        builder.addResponseCode(100);

        // test that caching works
        Assert.assertTrue(builder.build() == builder.build());

        Object original = builder.build();
        builder.addResponseCode(100);

        Assert.assertFalse(original == builder.build());
    }

    @Test
    public void testSanity() {
        ResponseStatisticsImpl.Builder builder = new ResponseStatisticsImpl.Builder();

        Assert.assertNull(builder.build().getLastResponseCode());

        builder.addResponseCode(100);

        Assert.assertEquals(100, (int) builder.build().getLastResponseCode());
        Assert.assertEquals(1, (long) builder.build().getResponseCodes().get(100));

        builder.addResponseCode(200);

        Assert.assertEquals(200, (int) builder.build().getLastResponseCode());
        Assert.assertEquals(1, (long) builder.build().getResponseCodes().get(100));
        Assert.assertEquals(1, (long) builder.build().getResponseCodes().get(200));

        builder.addResponseCode(100);

        Assert.assertEquals(100, (int) builder.build().getLastResponseCode());
        Assert.assertEquals(2, (long) builder.build().getResponseCodes().get(100));
        Assert.assertEquals(1, (long) builder.build().getResponseCodes().get(200));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testImmutability() {
        ResponseStatisticsImpl.Builder builder = new ResponseStatisticsImpl.Builder();

        builder.addResponseCode(100);

        // modification is not allowed
        exception.expect(UnsupportedOperationException.class);
        builder.build().getResponseCodes().put(100, 2L);
    }
}
