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

import java.text.ParseException;

import org.glassfish.jersey.internal.LocalizationMessages;

import static org.glassfish.jersey.message.internal.GrammarUtil.COMMENT;
import static org.glassfish.jersey.message.internal.GrammarUtil.CONTROL;
import static org.glassfish.jersey.message.internal.GrammarUtil.QUOTED_STRING;
import static org.glassfish.jersey.message.internal.GrammarUtil.SEPARATOR;
import static org.glassfish.jersey.message.internal.GrammarUtil.TOKEN;
import static org.glassfish.jersey.message.internal.GrammarUtil.filterToken;
import static org.glassfish.jersey.message.internal.GrammarUtil.getType;
import static org.glassfish.jersey.message.internal.GrammarUtil.isSeparator;
import static org.glassfish.jersey.message.internal.GrammarUtil.isToken;
import static org.glassfish.jersey.message.internal.GrammarUtil.isWhiteSpace;

/**
 * Concrete internal implementation of pull-based HTTP reader.
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
/* package */ final class HttpHeaderReaderImpl extends HttpHeaderReader {

    private final CharSequence header;
    private final boolean processComments;
    private final int length;

    private int index;
    private Event event;
    private CharSequence value;

    HttpHeaderReaderImpl(String header, boolean processComments) {
        this.header = (header == null) ? "" : header;
        this.processComments = processComments;
        this.index = 0;
        this.length = this.header.length();
    }

    HttpHeaderReaderImpl(String header) {
        this(header, false);
    }

    @Override
    public boolean hasNext() {
        return skipWhiteSpace();
    }

    @Override
    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (skipWhiteSpace) {
            skipWhiteSpace();
        }

        if (index >= length) {
            return false;
        }

        char c = header.charAt(index);
        return isSeparator(c) && c == separator;
    }

    @Override
    public String nextSeparatedString(char startSeparator, char endSeparator) throws ParseException {
        nextSeparator(startSeparator);
        final int start = index;
        for (; index < length; index++) {
            if (header.charAt(index) == endSeparator) {
                break;
            }
        }

        if (start == index) {
            // no token between separators
            throw new ParseException(LocalizationMessages.HTTP_HEADER_NO_CHARS_BETWEEN_SEPARATORS(startSeparator, endSeparator),
                    index);
        } else if (index == length) {
            // no end separator
            throw new ParseException(LocalizationMessages.HTTP_HEADER_NO_END_SEPARATOR(endSeparator), index);
        }

        event = Event.Token;
        value = header.subSequence(start, index++);
        return value.toString();
    }

    @Override
    public Event next() throws ParseException {
        return next(true);
    }

    @Override
    public Event next(boolean skipWhiteSpace) throws ParseException {
        return next(skipWhiteSpace, false);
    }

    @Override
    public Event next(boolean skipWhiteSpace, boolean preserveBackslash) throws ParseException {
        return event = process(getNextCharacter(skipWhiteSpace), preserveBackslash);
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public CharSequence getEventValue() {
        return value;
    }

    @Override
    public CharSequence getRemainder() {
        return (index < length) ? header.subSequence(index, header.length()) : null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    private boolean skipWhiteSpace() {
        for (; index < length; index++) {
            if (!isWhiteSpace(header.charAt(index))) {
                return true;
            }
        }

        return false;
    }

    private char getNextCharacter(boolean skipWhiteSpace) throws ParseException {
        if (skipWhiteSpace) {
            skipWhiteSpace();
        }

        if (index >= length) {
            throw new ParseException(LocalizationMessages.HTTP_HEADER_END_OF_HEADER(), index);
        }

        return header.charAt(index);
    }

    private Event process(char c, boolean preserveBackslash) throws ParseException {
        if (c > Byte.MAX_VALUE) {
            index++;
            return Event.Control;
        }

        switch (getType(c)) {
            case TOKEN: {
                final int start = index;
                for (index++; index < length; index++) {
                    if (!isToken(header.charAt(index))) {
                        break;
                    }
                }
                value = header.subSequence(start, index);
                return Event.Token;
            }
            case QUOTED_STRING:
                processQuotedString(preserveBackslash);
                return Event.QuotedString;
            case COMMENT:
                if (!processComments) {
                    throw new ParseException(LocalizationMessages.HTTP_HEADER_COMMENTS_NOT_ALLOWED(), index);
                }

                processComment();
                return Event.Comment;
            case SEPARATOR:
                index++;
                value = String.valueOf(c);
                return Event.Separator;
            case CONTROL:
                index++;
                value = String.valueOf(c);
                return Event.Control;
            default:
                // White space
                throw new ParseException(LocalizationMessages.HTTP_HEADER_WHITESPACE_NOT_ALLOWED(), index);
        }
    }

    private void processComment() throws ParseException {
        boolean filter = false;
        int nesting;
        int start;
        for (start = ++index, nesting = 1; nesting > 0 && index < length; index++) {
            char c = header.charAt(index);
            if (c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r') {
                filter = true;
            } else if (c == '(') {
                nesting++;
            } else if (c == ')') {
                nesting--;
            }
        }
        if (nesting != 0) {
            throw new ParseException(LocalizationMessages.HTTP_HEADER_UNBALANCED_COMMENTS(), index);
        }

        value = (filter) ? filterToken(header, start, index - 1) : header.subSequence(start, index - 1);
    }

    private void processQuotedString(boolean preserveBackslash) throws ParseException {
        boolean filter = false;
        for (int start = ++index; index < length; index++) {
            char c = this.header.charAt(index);
            if (!preserveBackslash && c == '\\') {
                index++;
                filter = true;
            } else if (c == '\r') {
                filter = true;
            } else if (c == '"') {
                value = (filter) ? filterToken(header, start, index, preserveBackslash) : header.subSequence(start, index);

                index++;
                return;
            }
        }

        throw new ParseException(LocalizationMessages.HTTP_HEADER_UNBALANCED_QUOTED(), index);
    }
}
