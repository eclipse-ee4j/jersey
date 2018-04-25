/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.nio.ByteBuffer;

/**
 * @author Alexey Stashok
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class HttpParserUtils {

    static final byte CR = (byte) '\r';
    static final byte LF = (byte) '\n';
    static final byte SP = (byte) ' ';
    static final byte HT = (byte) '\t';
    static final byte COMMA = (byte) ',';
    static final byte COLON = (byte) ':';
    static final byte SEMI_COLON = (byte) ';';
    static final byte A = (byte) 'A';
    static final byte Z = (byte) 'Z';
    static final byte a = (byte) 'a';
    static final byte LC_OFFSET = A - a;

    static int skipSpaces(ByteBuffer input, int offset, int packetLimit) {
        final int limit = Math.min(input.limit(), packetLimit);
        while (offset < limit) {
            final byte b = input.get(offset);
            if (isNotSpaceAndTab(b)) {
                return offset;
            }

            offset++;
        }

        return -1;
    }

    static boolean isNotSpaceAndTab(byte b) {
        return !isSpaceOrTab(b);
    }

    static boolean isSpaceOrTab(byte b) {
        return (b == HttpParserUtils.SP || b == HttpParserUtils.HT);
    }

    static class HeaderParsingState {

        final int maxHeaderSize;
        int packetLimit;

        int state;
        int subState;

        int start;
        int offset;
        int checkpoint = -1; // extra parsing state field
        int checkpoint2 = -1; // extra parsing state field

        String headerName;

        long parsingNumericValue;

        int contentLengthHeadersCount;   // number of Content-Length headers in the HTTP header
        boolean contentLengthsDiffer;

        HeaderParsingState(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
        }

        void recycle() {
            state = 0;
            subState = 0;
            start = 0;
            offset = 0;
            checkpoint = -1;
            checkpoint2 = -1;
            parsingNumericValue = 0;
            contentLengthHeadersCount = 0;
            contentLengthsDiffer = false;
            headerName = null;
            packetLimit = maxHeaderSize;
        }

        void checkOverflow(String errorDescriptionIfOverflow) throws ParseException {
            if (offset < packetLimit) {
                return;
            }

            throw new ParseException(errorDescriptionIfOverflow);
        }
    }

    static class ContentParsingState {

        boolean isLastChunk;
        int chunkContentStart = -1;
        long chunkLength = -1;
        long chunkRemainder = -1;
    }
}
