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
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.EntityTag;

import org.glassfish.jersey.internal.LocalizationMessages;

/**
 * A matching entity tag.
 * <p>
 * Note that this type and it's super type cannot be used to create request
 * header values for {@code If-Match} and {@code If-None-Match}
 * of the form {@code If-Match: *} or {@code If-None-Match: *} as
 * {@code *} is not a valid entity tag.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MatchingEntityTag extends EntityTag {

    /**
     * An empty set that corresponds to {@code If-Match: *} or {@code If-None-Match: *}.
     */
    public static final Set<MatchingEntityTag> ANY_MATCH = Collections.emptySet();

    /**
     * Create new strongly validating entity tag.
     *
     * @param value ETag header value.
     */
    public MatchingEntityTag(String value) {
        super(value, false);
    }

    /**
     * Create new matching entity tag.
     *
     * @param value ETag header value.
     * @param weak should be set to false, if strong validation is required,
     *            otherwise should be set to true.
     */
    public MatchingEntityTag(String value, boolean weak) {
        super(value, weak);
    }

    /**
     * Create new matching entity tag out of provided header reader.
     *
     * @param reader HTTP header content reader.
     * @return a new matching entity tag.
     * @throws ParseException in case the header could not be parsed.
     */
    public static MatchingEntityTag valueOf(HttpHeaderReader reader) throws ParseException {
        final CharSequence tagString = reader.getRemainder();

        HttpHeaderReader.Event e = reader.next(false);
        if (e == HttpHeaderReader.Event.QuotedString) {
            return new MatchingEntityTag(reader.getEventValue().toString());
        } else if (e == HttpHeaderReader.Event.Token) {
            CharSequence ev = reader.getEventValue();
            if (ev != null && ev.length() == 1 && 'W' == ev.charAt(0)) {
                reader.nextSeparator('/');
                return new MatchingEntityTag(reader.nextQuotedString().toString(), true);
            }
        }

        throw new ParseException(LocalizationMessages.ERROR_PARSING_ENTITY_TAG(tagString), reader.getIndex());
    }
}
