/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.QualitySourceMediaType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Quality source media type unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@RunWith(Parameterized.class)
public class QualitySourceMediaTypeTest {
    @Parameterized.Parameters
    // expected result, media type, quality source media type
    public static List<Object[]> testBeds() {
        return Arrays.asList(new Object[][]{
                {Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "json")},
                {Boolean.TRUE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "json", 1000, null)},
                {Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "json", 500, null)},
                {Boolean.FALSE, MediaType.APPLICATION_JSON_TYPE, new QualitySourceMediaType("application", "xml")}
        });
    }

    private final boolean expectEquality;
    private final MediaType mediaType;
    private final QualitySourceMediaType qsMediaType;

    public QualitySourceMediaTypeTest(boolean expectEquality, MediaType mediaType, QualitySourceMediaType qsMediaType) {
        this.expectEquality = expectEquality;
        this.mediaType = mediaType;
        this.qsMediaType = qsMediaType;
    }

    @Test
    public void testEquals() throws Exception {
        if (expectEquality) {
            Assert.assertEquals("Types not equal.", mediaType, qsMediaType);
            Assert.assertEquals("Types not equal.", qsMediaType, mediaType);
            Assert.assertEquals(
                    String.format("Hash codes not equal for %s and %s.", mediaType.toString(), qsMediaType.toString()),
                    mediaType.hashCode(), qsMediaType.hashCode());
        } else {
            Assert.assertFalse(String.format("False equality of %s and %s", mediaType.toString(), qsMediaType.toString()),
                    qsMediaType.equals(mediaType));
        }
    }
}
