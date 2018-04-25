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

import javax.ws.rs.core.Cookie;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * Request {@code Cookie} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public class CookieProvider implements HeaderDelegateProvider<Cookie> {

    @Override
    public boolean supports(Class<?> type) {
        return type == Cookie.class;
    }

    @Override
    public String toString(Cookie cookie) {

        throwIllegalArgumentExceptionIfNull(cookie, LocalizationMessages.COOKIE_IS_NULL());

        StringBuilder b = new StringBuilder();

        b.append("$Version=").append(cookie.getVersion()).append(';');

        b.append(cookie.getName()).append('=');
        StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getValue());

        if (cookie.getDomain() != null) {
            b.append(";$Domain=");
            StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            b.append(";$Path=");
            StringBuilderUtils.appendQuotedIfWhitespace(b, cookie.getPath());
        }
        return b.toString();
    }

    @Override
    public Cookie fromString(String header) {
        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.COOKIE_IS_NULL());
        return HttpHeaderReader.readCookie(header);
    }
}
