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

package org.glassfish.jersey.internal.util;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tokenizer utility unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class TokenizerTest {
    @Test
    public void testTokenizeArrayCommonDelimiters() {
        final String[] expected = new String[] {"ab", "c", "d"};
        final String[] input = new String[] {"ab c", "", null, "  d"};

        final String[] actual = Tokenizer.tokenize(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testTokenizeArrayCustomDelimiters() throws Exception {
        final String[] expected = new String[] {"ab", "c", "d"};
        final String[] input = new String[] {"abkc", "k", null, " k d"};

        final String[] actual = Tokenizer.tokenize(input, "k");

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testTokenizeCommonDelimiters() throws Exception {
        final String[] expected = new String[] {"ab", "c", "d"};
        final String input = "ab c  d";

        final String[] actual = Tokenizer.tokenize(input);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testTokenizeCustomDelimiters() throws Exception {
        final String[] expected = new String[] {"ab", "c", "d"};
        final String input = "abkckkd";

        final String[] actual = Tokenizer.tokenize(input, "k");

        assertArrayEquals(expected, actual);
    }

}
