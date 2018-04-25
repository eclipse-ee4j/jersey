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
import java.util.Date;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * {@code Date} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class DateProvider implements HeaderDelegateProvider<Date> {

    @Override
    public boolean supports(final Class<?> type) {
        return Date.class.isAssignableFrom(type);
    }

    @Override
    public String toString(final Date header) {
        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.DATE_IS_NULL());
        return HttpDateFormat.getPreferredDateFormat().format(header);
    }

    @Override
    public Date fromString(final String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.DATE_IS_NULL());

        try {
            return HttpHeaderReader.readDate(header);
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(
                    "Error parsing date '" + header + "'", ex);
        }
    }
}
