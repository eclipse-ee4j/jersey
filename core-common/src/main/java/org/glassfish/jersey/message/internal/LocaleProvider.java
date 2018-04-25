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
import java.util.Locale;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * {@code Locale} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class LocaleProvider implements HeaderDelegateProvider<Locale> {

    @Override
    public boolean supports(final Class<?> type) {
        return Locale.class.isAssignableFrom(type);
    }

    @Override
    public String toString(final Locale header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.LOCALE_IS_NULL());

        if (header.getCountry().length() == 0) {
            return header.getLanguage();
        } else {
            return header.getLanguage() + '-' + header.getCountry();
        }
    }

    @Override
    public Locale fromString(final String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.LOCALE_IS_NULL());

        try {
            final LanguageTag lt = new LanguageTag(header);
            return lt.getAsLocale();
        } catch (final ParseException ex) {
            throw new IllegalArgumentException(
                    "Error parsing date '" + header + "'", ex);
        }
    }
}
