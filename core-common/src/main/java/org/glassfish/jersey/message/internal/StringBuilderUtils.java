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

package org.glassfish.jersey.message.internal;

/**
 * Internal {@link StringBuilder string builder} utilities for building HTTP header
 * values.
 *
 * @author Marc Hadley
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Martin Matula
 */
public class StringBuilderUtils {

    /**
     * Append a new value to the string builder.
     *
     * If the value contains non-token characters (e.g. control, white-space,
     * quotes, separators, etc.), the appended value is quoted and all the quotes
     * in the value are escaped.
     *
     * @param b string builder to be updated.
     * @param value value to be appended.
     */
    public static void appendQuotedIfNonToken(StringBuilder b, String value) {
        if (value == null) {
            return;
        }
        boolean quote = !GrammarUtil.isTokenString(value);
        if (quote) {
            b.append('"');
        }
        appendEscapingQuotes(b, value);
        if (quote) {
            b.append('"');
        }
    }

    /**
     * Append a new value to the string builder.
     *
     * If the value contains white-space characters, the appended
     * value is quoted and all the quotes in the value are escaped.
     *
     * @param b string builder to be updated.
     * @param value value to be appended.
     */
    public static void appendQuotedIfWhitespace(StringBuilder b, String value) {
        if (value == null) {
            return;
        }
        boolean quote = GrammarUtil.containsWhiteSpace(value);
        if (quote) {
            b.append('"');
        }
        appendEscapingQuotes(b, value);
        if (quote) {
            b.append('"');
        }
    }

    /**
     * Append a new quoted value to the string builder.
     *
     * The appended value is quoted and all the quotes in the value are escaped.
     *
     * @param b string builder to be updated.
     * @param value value to be appended.
     */
    public static void appendQuoted(StringBuilder b, String value) {
        b.append('"');
        appendEscapingQuotes(b, value);
        b.append('"');
    }

    /**
     * Append a new value to the string builder.
     *
     * All the quotes in the value are escaped before appending.
     *
     * @param b string builder to be updated.
     * @param value value to be appended.
     */
    public static void appendEscapingQuotes(StringBuilder b, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                b.append('\\');
            }
            b.append(c);
        }
    }

    /**
     * Prevents instantiation.
     */
    private StringBuilderUtils() {
    }
}
