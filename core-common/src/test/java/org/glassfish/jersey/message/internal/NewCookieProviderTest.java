/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import jakarta.ws.rs.core.NewCookie;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class NewCookieProviderTest {

    private final NewCookie newCookie = new NewCookie(
            "test",
            "value",
            "/",
            "localhost",
            1,
            "comment",
            60,
            new Date(),
            true,
            true,
            NewCookie.SameSite.STRICT
    );

    @Test
    public void SameSiteTest() {
        final NewCookieProvider provider = new NewCookieProvider();
        final String newCookieString = provider.toString(newCookie);
        Assert.assertTrue(newCookieString.contains("SameSite=STRICT"));
        Assert.assertEquals(NewCookie.SameSite.STRICT, provider.fromString(newCookieString).getSameSite());
    }

}
