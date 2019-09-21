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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Martin Matula
 */
public class StringBuilderUtilsTest {

    public StringBuilderUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testAppendQuotedIfNonToken() {
        StringBuilder sb = new StringBuilder();
        StringBuilderUtils.appendQuotedIfNonToken(sb, "a:b");
        assertEquals("\"a:b\"", sb.toString());
        sb = new StringBuilder();
        StringBuilderUtils.appendQuotedIfNonToken(sb, "abc");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testAppendQuotedIfWhitespace() {
        StringBuilder sb = new StringBuilder();
        StringBuilderUtils.appendQuotedIfWhitespace(sb, "a b");
        assertEquals("\"a b\"", sb.toString());
        sb = new StringBuilder();
        StringBuilderUtils.appendQuotedIfWhitespace(sb, "a:b");
        assertEquals("a:b", sb.toString());
    }

    @Test
    public void testAppendQuoted() {
        StringBuilder sb = new StringBuilder();
        StringBuilderUtils.appendQuoted(sb, "abc");
        assertEquals("\"abc\"", sb.toString());
    }

    @Test
    public void testAppendEscapingQuotes() {
        StringBuilder sb = new StringBuilder();
        StringBuilderUtils.appendEscapingQuotes(sb, "a\"b");
        assertEquals("a\\\"b", sb.toString());
        sb = new StringBuilder();
        StringBuilderUtils.appendEscapingQuotes(sb, "abc");
        assertEquals("abc", sb.toString());
    }
}
