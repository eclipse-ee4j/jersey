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

package org.glassfish.jersey.message.internal;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: javadoc.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class VariantListBuilderTest {
    @Test
    public void testAddAndNoAddBeforeBuild() throws Exception {
        final Variant v1 = new Variant(MediaType.TEXT_PLAIN_TYPE, Locale.ENGLISH, null);
        final Variant v2 = new Variant(MediaType.TEXT_PLAIN_TYPE, Locale.FRENCH, null);
        final Variant v3 = new Variant(MediaType.TEXT_HTML_TYPE, Locale.ENGLISH, null);
        final Variant v4 = new Variant(MediaType.TEXT_HTML_TYPE, Locale.FRENCH, null);

        List<Variant> variants;
        variants = new VariantListBuilder()
                .mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE).languages(Locale.ENGLISH, Locale.FRENCH)
                .add()
                .build();

        assertEquals(4, variants.size());
        assertTrue(variants.contains(v1));
        assertTrue(variants.contains(v2));
        assertTrue(variants.contains(v3));
        assertTrue(variants.contains(v4));

        variants = new VariantListBuilder()
                .mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE).languages(Locale.ENGLISH, Locale.FRENCH)
                .build();

        assertEquals(4, variants.size());
        assertTrue(variants.contains(v1));
        assertTrue(variants.contains(v2));
        assertTrue(variants.contains(v3));
        assertTrue(variants.contains(v4));
    }
}
