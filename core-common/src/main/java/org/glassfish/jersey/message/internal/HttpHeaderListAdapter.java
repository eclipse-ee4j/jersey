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

/**
 * Wrapping adapter for {@link HttpHeaderReader} that adds ability to read
 * headers containing comma-separated value lists.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
/* package */ class HttpHeaderListAdapter extends HttpHeaderReader {

    private final HttpHeaderReader reader;
    private boolean isTerminated;

    /**
     * Create new adapter for {@link HttpHeaderReader} that adds ability to read
     * headers containing comma-separated value lists.
     *
     * @param reader http header reader to be wrapped.
     */
    public HttpHeaderListAdapter(HttpHeaderReader reader) {
        this.reader = reader;
    }

    public void reset() {
        isTerminated = false;
    }

    @Override
    public boolean hasNext() {
        if (isTerminated) {
            return false;
        }

        if (reader.hasNext()) {
            if (reader.hasNextSeparator(',', true)) {
                isTerminated = true;
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (isTerminated) {
            return false;
        }

        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            return false;
        } else {
            return reader.hasNextSeparator(separator, skipWhiteSpace);
        }
    }

    @Override
    public Event next() throws ParseException {
        return next(true);
    }

    @Override
    public HttpHeaderReader.Event next(boolean skipWhiteSpace) throws ParseException {
        return next(skipWhiteSpace, false);
    }

    @Override
    public HttpHeaderReader.Event next(boolean skipWhiteSpace, boolean preserveBackslash) throws ParseException {
        if (isTerminated) {
            throw new ParseException("End of header", getIndex());
        }

        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }

        return reader.next(skipWhiteSpace, preserveBackslash);
    }

    @Override
    public CharSequence nextSeparatedString(char startSeparator, char endSeparator) throws ParseException {
        if (isTerminated) {
            throw new ParseException("End of header", getIndex());
        }

        if (reader.hasNextSeparator(',', true)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }

        return reader.nextSeparatedString(startSeparator, endSeparator);
    }

    @Override
    public HttpHeaderReader.Event getEvent() {
        return reader.getEvent();
    }

    @Override
    public CharSequence getEventValue() {
        return reader.getEventValue();
    }

    @Override
    public CharSequence getRemainder() {
        return reader.getRemainder();
    }

    @Override
    public int getIndex() {
        return reader.getIndex();
    }
}
