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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A utility class providing methods capable of splitting String entries
 * into an array of tokens based on either default or custom token delimiters.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class Tokenizer {
    private Tokenizer() {
        // prevents instantiation.
    }

    /**
     * Common Jersey delimiters used by various properties.
     */
    public static final String COMMON_DELIMITERS = " ,;\n";

    /**
     * Get a canonical array of tokens from an array of String entries
     * where each entry may contain zero or more tokens separated by
     * common delimiters {@code " ,;\n"}.
     *
     * @param entries an array where each String entry may contain zero or more
     *                {@link #COMMON_DELIMITERS common delimiters} separated tokens.
     * @return the array of tokens, each token is trimmed, the array will
     *         not contain any empty or {@code null} entries.
     */
    public static String[] tokenize(final String[] entries) {
        return tokenize(entries, COMMON_DELIMITERS);
    }

    /**
     * Get a canonical array of tokens from an array of String entries
     * where each entry may contain zero or more tokens separated by characters
     * in delimiters string.
     *
     * @param entries    an array where each String entry may contain zero or more
     *                   delimiters separated tokens.
     * @param delimiters string with delimiters, every character represents one
     *                   delimiter.
     * @return the array of tokens, each token is trimmed, the array will
     *         not contain any empty or {@code null} entries.
     */
    public static String[] tokenize(final String[] entries, final String delimiters) {
        final List<String> tokens = new LinkedList<String>();

        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }
            tokenize(entry, delimiters, tokens);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Get a canonical array of tokens from a String entry that may contain
     * zero or more tokens separated by common delimiters {@code " ,;\n"}.
     *
     * @param entry a String that may contain zero or more
     *              {@link #COMMON_DELIMITERS common delimiters} separated tokens.
     * @return the array of tokens, each tokens is trimmed, the array will
     *         not contain any empty or {@code null} entries.
     */
    public static String[] tokenize(final String entry) {
        return tokenize(entry, COMMON_DELIMITERS);
    }

    /**
     * Get a canonical array of tokens from a String entry
     * that may contain zero or more tokens separated by characters in
     * delimiters string.
     *
     * @param entry      a String that may contain zero or more
     *                   delimiters separated tokens.
     * @param delimiters string with delimiters, every character represents one
     *                   delimiter.
     * @return the array of tokens, each tokens is trimmed, the array will
     *         not contain any empty or {@code null} entries.
     */
    public static String[] tokenize(final String entry, final String delimiters) {
        final Collection<String> tokens = tokenize(entry, delimiters, new LinkedList<String>());
        return tokens.toArray(new String[tokens.size()]);
    }

    private static Collection<String> tokenize(final String entry, final String delimiters, final Collection<String> tokens) {
        final StringBuilder regexpBuilder = new StringBuilder(delimiters.length() * 3);
        regexpBuilder.append('[');
        for (final char c : delimiters.toCharArray()) {
            regexpBuilder.append(Pattern.quote(String.valueOf(c)));
        }
        regexpBuilder.append(']');

        final String[] tokenArray = entry.split(regexpBuilder.toString());
        for (String token : tokenArray) {
            if (token == null || token.isEmpty()) {
                continue;
            }

            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }

            tokens.add(token);
        }

        return tokens;
    }
}
