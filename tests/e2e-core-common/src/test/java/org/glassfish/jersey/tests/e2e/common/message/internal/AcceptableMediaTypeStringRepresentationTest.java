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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.MediaTypeProvider;
import org.glassfish.jersey.message.internal.Quality;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Acceptable media type unit tests.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@RunWith(Parameterized.class)
public class AcceptableMediaTypeStringRepresentationTest {
    @Parameterized.Parameters
    // expected result, acceptable media type
    public static List<Object[]> getParameters() {
        final Map<String, String> emptyParams = new HashMap<String, String>();
        final Map<String, String> params = new HashMap<String, String>();
        params.put("myParam", "myValue");

        return Arrays.asList(new Object[][]{
                {"*/*", new AcceptableMediaType("*", "*")},
                {"*/*", new AcceptableMediaType("*", "*", Quality.DEFAULT, emptyParams)},
                {"*/*;q=0.75", new AcceptableMediaType("*", "*", 750, emptyParams)},
                {"text/html", new AcceptableMediaType("text", "html", Quality.DEFAULT, null)},
                {"text/html;q=0.5", new AcceptableMediaType("text", "html", 500, emptyParams)},
                {"image/*;myparam=myValue;q=0.8", new AcceptableMediaType("image", "*", 800, params)},
        });
    }

    private final String expectedValue;
    private final AcceptableMediaType testedType;

    public AcceptableMediaTypeStringRepresentationTest(final String expectedValue,
                                                       final AcceptableMediaType testedType) {
        this.expectedValue = expectedValue;
        this.testedType = testedType;
    }

    @Test
    public void testStringRepresentation() {
        final MediaTypeProvider provider = new MediaTypeProvider();
        Assert.assertEquals(expectedValue, testedType.toString());
        provider.fromString(testedType.toString());
    }
}
