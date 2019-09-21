/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Class containing static utility methods to work with HTTP headers.
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
final class GrammarUtil {

    /**
     * Represents token type in the TYPE_TABLE.
     */
    public static final int TOKEN = 0;
    /**
     * Represents quotes type in the TYPE_TABLE.
     */
    public static final int QUOTED_STRING = 1;
    /**
     * Represents comment type in the TYPE_TABLE.
     */
    public static final int COMMENT = 2;
    /**
     * Represents separator type in the TYPE_TABLE.
     */
    public static final int SEPARATOR = 3;
    /**
     * Represents control type in the TYPE_TABLE.
     */
    public static final int CONTROL = 4;
    /**
     * Array of chars representing white spaces.
     */
    private static final char[] WHITE_SPACE = {'\t', '\r', '\n', ' '};
    /**
     * Array of chars representing separators.
     */
    private static final char[] SEPARATORS =
            {'(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t'};
    /**
     * Mapping of chars to types.
     */
    private static final int[] TYPE_TABLE = createEventTable();
    /**
     * Convenience table mapping chars to true if they are white space chars.
     */
    private static final boolean[] IS_WHITE_SPACE = createWhiteSpaceTable();
    /**
     * convenience table mapping chars to true if they are tokens.
     */
    private static final boolean[] IS_TOKEN = createTokenTable();

    private static int[] createEventTable() {
        int[] table = new int[Byte.MAX_VALUE + 1];

        // Control
        final int control_char_bound = 32;
        for (int i = 0; i < control_char_bound; i++) {
            table[i] = CONTROL;
        }
        table[Byte.MAX_VALUE] = CONTROL;

        // Token
        for (int i = control_char_bound; i < Byte.MAX_VALUE; i++) {
            table[i] = TOKEN;
        }

        // Separator
        for (char c : SEPARATORS) {
            table[c] = SEPARATOR;
        }

        // Comment
        table['('] = COMMENT;

        // QuotedString
        table['"'] = QUOTED_STRING;

        // White space
        for (char c : WHITE_SPACE) {
            table[c] = -1;
        }

        return table;
    }

    private static boolean[] createWhiteSpaceTable() {
        boolean[] table = new boolean[Byte.MAX_VALUE + 1];

        for (char c : WHITE_SPACE) {
            table[c] = true;
        }

        return table;
    }

    private static boolean[] createTokenTable() {
        boolean[] table = new boolean[Byte.MAX_VALUE + 1];

        for (int i = 0; i <= Byte.MAX_VALUE; i++) {
            table[i] = (TYPE_TABLE[i] == TOKEN);
        }

        return table;
    }

    /**
     * Returns {@code true} if the provided char is a white space.
     *
     * @param c char to check.
     * @return {@code true} if c is a white space.
     */
    public static boolean isWhiteSpace(final char c) {
        return (c <= Byte.MAX_VALUE && IS_WHITE_SPACE[c]);
    }

    /**
     * Returns {@code true} if the provided char is a token.
     *
     * @param c char to check.
     * @return {@code true} if c is a token.
     */
    public static boolean isToken(final char c) {
        return (c <= Byte.MAX_VALUE && IS_TOKEN[c]);
    }

    /**
     * Get the character type.
     *
     * @param c char to check.
     * @return character type identifier.
     *
     * @throws IllegalArgumentException in case the character value is greater than 127.
     */
    public static int getType(final char c) {
        if (c > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Unsupported character - ordinal value too high: " + c);
        }
        return TYPE_TABLE[c];
    }

    /**
     * Returns {@code true} if the provided char is a separator.
     *
     * @param c char to check.
     * @return {@code true} if c is a token.
     */
    public static boolean isSeparator(final char c) {
        return (c <= Byte.MAX_VALUE && TYPE_TABLE[c] == SEPARATOR);
    }

    /**
     * Returns {@code true} if all chars in string s are tokens.
     *
     * @param s string to check for tokens.
     * @return {@code true} if all chars in s are tokens.
     */
    public static boolean isTokenString(final String s) {
        for (char c : s.toCharArray()) {
            if (!isToken(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if string s contains a white space char.
     *
     * @param s string to check for white spaces.
     * @return {@code true} if s contains white spaces.
     */
    public static boolean containsWhiteSpace(final String s) {
        for (char c : s.toCharArray()) {
            if (isWhiteSpace(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filter a substring of a string by removing any new-line characters and
     * un-escaping escaped characters.
     *
     * @param s     character sequence to use for substring token filtering.
     * @param start start filtering position in the string.
     * @param end   end filtering position in the string.
     * @return filtered substring.
     */
    public static String filterToken(final CharSequence s, final int start, final int end) {
        return filterToken(s, start, end, false);
    }

    /**
     * Filter a substring of a string by removing any new-line characters and
     * un-escaping escaped characters (unless preserveBackslash is set to {@code true}).
     *
     * @param s                 character sequence to use for substring token filtering.
     * @param start             start filtering position in the string.
     * @param end               end filtering position in the string.
     * @param preserveBackslash if set to {@code true}, this method does not treat backslash as an escape character
     *                          (treats it as a regular character instead)
     * @return filtered substring.
     */
    public static String filterToken(final CharSequence s, final int start, final int end, final boolean preserveBackslash) {
        StringBuilder sb = new StringBuilder();
        char c;
        boolean gotEscape = false;
        boolean gotCR = false;

        for (int i = start; i < end; i++) {
            c = s.charAt(i);
            if (c == '\n' && gotCR) {
                // This LF is part of an unescaped
                // CRLF sequence (i.e, LWSP). Skip it.
                gotCR = false;
                continue;
            }

            gotCR = false;
            if (!gotEscape) {
                // Previous character was NOT '\'
                if (!preserveBackslash && c == '\\') { // skip this character
                    gotEscape = true;
                } else if (c == '\r') { // skip this character
                    gotCR = true;
                } else { // append this character
                    sb.append(c);
                }
            } else {
                // Previous character was '\'. So no need to
                // bother with any special processing, just
                // append this character
                sb.append(c);
                gotEscape = false;
            }
        }
        return sb.toString();
    }

    /**
     * Prevents instantiation.
     */
    private GrammarUtil() {
    }
}
