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

import javax.ws.rs.core.NewCookie;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * Response {@code Set-Cookie} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class NewCookieProvider implements HeaderDelegateProvider<NewCookie> {

    @Override
    public boolean supports(final Class<?> type) {
        return type == NewCookie.class;
    }

    @Override
    public String toString(final NewCookie cookie) {

        throwIllegalArgumentExceptionIfNull(cookie, LocalizationMessages.NEW_COOKIE_IS_NULL());

        final StringBuilder b = new StringBuilder();

        b.append(cookie.getName()).append('=');
        StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getValue());

        b.append(";").append("Version=").append(cookie.getVersion());

        if (cookie.getComment() != null) {
            b.append(";Comment=");
            StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getComment());
        }
        if (cookie.getDomain() != null) {
            b.append(";Domain=");
            StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            b.append(";Path=");
            StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getPath());
        }
        if (cookie.getMaxAge() != -1) {
            b.append(";Max-Age=");
            b.append(cookie.getMaxAge());
        }
        if (cookie.isSecure()) {
            b.append(";Secure");
        }
        if (cookie.isHttpOnly()) {
            b.append(";HttpOnly");
        }
        if (cookie.getExpiry() != null) {
            b.append(";Expires=");
            b.append(HttpDateFormat.getPreferredDateFormat().format(cookie.getExpiry()));
        }

        return b.toString();
    }

    @Override
    public NewCookie fromString(final String header) {
        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.NEW_COOKIE_IS_NULL());
        return HttpHeaderReader.readNewCookie(header);
    }
}
