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

import javax.ws.rs.core.EntityTag;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.message.internal.HttpHeaderReader.Event;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * {@code ETag} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Marc Hadley
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class EntityTagProvider implements HeaderDelegateProvider<EntityTag> {

    @Override
    public boolean supports(Class<?> type) {
        return type == EntityTag.class;
    }

    @Override
    public String toString(EntityTag header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.ENTITY_TAG_IS_NULL());

        StringBuilder b = new StringBuilder();
        if (header.isWeak()) {
            b.append("W/");
        }
        StringBuilderUtils.appendQuoted(b, header.getValue());
        return b.toString();
    }

    @Override
    public EntityTag fromString(String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.ENTITY_TAG_IS_NULL());

        try {
            HttpHeaderReader reader = HttpHeaderReader.newInstance(header);
            Event e = reader.next(false);
            if (e == Event.QuotedString) {
                return new EntityTag(reader.getEventValue().toString());
            } else if (e == Event.Token) {
                final CharSequence ev = reader.getEventValue();
                if (ev != null && ev.length() > 0 && ev.charAt(0) == 'W') {
                    reader.nextSeparator('/');
                    return new EntityTag(reader.nextQuotedString().toString(), true);
                }
            }
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                    "Error parsing entity tag '" + header + "'", ex);
        }

        throw new IllegalArgumentException(
                "Error parsing entity tag '" + header + "'");
    }
}
