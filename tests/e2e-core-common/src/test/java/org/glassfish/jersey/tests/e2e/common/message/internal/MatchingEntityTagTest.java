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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.text.ParseException;
import java.util.Set;

import javax.ws.rs.core.EntityTag;

import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.message.internal.MatchingEntityTag;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link MatchingEntityTag} unit tests ported from Jersey 1.x.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MatchingEntityTagTest {

    @Test
    public void testOneEntityTag() throws Exception {
        String header = "\"1\"";
        Set<MatchingEntityTag> s = HttpHeaderReader.readMatchingEntityTag(header);

        assertEquals(1, s.size());

        assertTrue(s.contains(new EntityTag("1")));
    }

    @Test
    public void testMultipleEntityTag() throws Exception {
        String header = "\"1\", W/\"2\", \"3\"";
        Set<MatchingEntityTag> s = HttpHeaderReader.readMatchingEntityTag(header);

        assertEquals(3, s.size());

        assertTrue(s.contains(new EntityTag("1")));

        assertTrue(s.contains(new EntityTag("2", true)));

        assertTrue(s.contains(new EntityTag("3")));
    }

    @Test
    public void testAnyMatch() throws Exception {
        String header = "*";
        Set<MatchingEntityTag> s = HttpHeaderReader.readMatchingEntityTag(header);

        assertThat(s.size(), is(equalTo(0)));
        assertThat(MatchingEntityTag.ANY_MATCH, is(s));
    }

    /**
     * Reproducer for JERSEY-1278.
     */
    @Test
    public void testBadEntityTag() {
        String header = "1\"";
        try {
            HttpHeaderReader.readMatchingEntityTag(header);
            fail("ParseException expected");
        } catch (ParseException pe) {
            assertThat(pe.getMessage(), containsString(header));
        }
    }
}
