/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Locale;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link LanguageTag} class.
 *
 * @author Michal Gajdos
 */
public class LanguageTagTest {

    @Test
    public void testLanguageCountry() throws Exception {
        _test("en", "gb");
        _test("sk", "SK");
        _test("CZ", "cs");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLanguageCountryInvalid() throws Exception {
        _test("en", "gbgbgbgbgb");
    }

    @Test
    public void testLanguageRegion() throws Exception {
        _test("es", "419");
    }

    @Test
    public void testEquals() {
        LanguageTag lt1 = new LanguageTag("en", "us");
        LanguageTag lt2 = new LanguageTag("en", "us");

        assertTrue(lt1.equals(lt2));
    }

    @Test
    public void testNonEquals() {
        LanguageTag lt1 = new LanguageTag("en", "us");
        LanguageTag lt2 = new LanguageTag("en", "gb");

        assertFalse(lt1.equals(lt2));
    }

    private void _test(final String primary, final String sub) throws Exception {
        final LanguageTag tag = LanguageTag.valueOf(primary + "-" + sub);

        assertThat(tag.getPrimaryTag(), equalToIgnoringCase(primary));
        assertThat(tag.getSubTags(), equalToIgnoringCase(sub));

        final Locale locale = tag.getAsLocale();

        assertThat(locale.getLanguage(), equalToIgnoringCase(primary));
        assertThat(locale.getCountry(), equalToIgnoringCase(sub));
    }
}
