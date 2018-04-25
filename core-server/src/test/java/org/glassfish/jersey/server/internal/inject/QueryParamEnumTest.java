/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class QueryParamEnumTest extends AbstractTest {

    public static enum Days {
        MON, TUE, WED, THU, FRI, SAT, SUN;

        public static Days fromString(String s) {
            return valueOf(s.toUpperCase());
        }
    }

    @Path("/")
    public static class ResourceString {
        @GET
        public String doGet(
                @QueryParam("arg1") Days d) {
            assertEquals(Days.MON, d);
            return "content";
        }
    }

    @Test
    public void testEnumGet() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceString.class);

        _test("/?arg1=MON");
        _test("/?arg1=mon");
    }
}
