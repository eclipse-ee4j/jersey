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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.CacheControl;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;
import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * {@code Cache-Control} {@link HeaderDelegateProvider header delegate provider}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author hubick@java.net
 */
@Singleton
public final class CacheControlProvider implements HeaderDelegateProvider<CacheControl> {

    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("[\\s]*,[\\s]*");

    @Override
    public boolean supports(Class<?> type) {
        return type == CacheControl.class;
    }

    @Override
    public String toString(CacheControl header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.CACHE_CONTROL_IS_NULL());

        StringBuilder b = new StringBuilder();
        if (header.isPrivate()) {
            appendQuotedWithSeparator(b, "private", buildListValue(header.getPrivateFields()));
        }
        if (header.isNoCache()) {
            appendQuotedWithSeparator(b, "no-cache", buildListValue(header.getNoCacheFields()));
        }
        if (header.isNoStore()) {
            appendWithSeparator(b, "no-store");
        }
        if (header.isNoTransform()) {
            appendWithSeparator(b, "no-transform");
        }
        if (header.isMustRevalidate()) {
            appendWithSeparator(b, "must-revalidate");
        }
        if (header.isProxyRevalidate()) {
            appendWithSeparator(b, "proxy-revalidate");
        }
        if (header.getMaxAge() != -1) {
            appendWithSeparator(b, "max-age", header.getMaxAge());
        }
        if (header.getSMaxAge() != -1) {
            appendWithSeparator(b, "s-maxage", header.getSMaxAge());
        }

        for (Map.Entry<String, String> e : header.getCacheExtension().entrySet()) {
            appendWithSeparator(b, e.getKey(), quoteIfWhitespace(e.getValue()));
        }

        return b.toString();
    }

    private void readFieldNames(List<String> fieldNames, HttpHeaderReader reader)
            throws ParseException {
        if (!reader.hasNextSeparator('=', false)) {
            return;
        }
        reader.nextSeparator('=');
        fieldNames.addAll(Arrays.asList(COMMA_SEPARATED_LIST.split(reader.nextQuotedString())));
    }

    private int readIntValue(HttpHeaderReader reader, String directiveName)
            throws ParseException {
        reader.nextSeparator('=');
        int index = reader.getIndex();
        try {
            return Integer.parseInt(reader.nextToken().toString());
        } catch (NumberFormatException nfe) {
            ParseException pe = new ParseException(
                    "Error parsing integer value for " + directiveName + " directive", index);
            pe.initCause(nfe);
            throw pe;
        }
    }

    private void readDirective(CacheControl cacheControl,
                               HttpHeaderReader reader) throws ParseException {

        final String directiveName = reader.nextToken().toString().toLowerCase();
        if ("private".equals(directiveName)) {
            cacheControl.setPrivate(true);
            readFieldNames(cacheControl.getPrivateFields(), reader);
        } else if ("public".equals(directiveName)) {
            // CacheControl API doesn't support 'public' for some reason.
            cacheControl.getCacheExtension().put(directiveName, null);
        } else if ("no-cache".equals(directiveName)) {
            cacheControl.setNoCache(true);
            readFieldNames(cacheControl.getNoCacheFields(), reader);
        } else if ("no-store".equals(directiveName)) {
            cacheControl.setNoStore(true);
        } else if ("no-transform".equals(directiveName)) {
            cacheControl.setNoTransform(true);
        } else if ("must-revalidate".equals(directiveName)) {
            cacheControl.setMustRevalidate(true);
        } else if ("proxy-revalidate".equals(directiveName)) {
            cacheControl.setProxyRevalidate(true);
        } else if ("max-age".equals(directiveName)) {
            cacheControl.setMaxAge(readIntValue(reader, directiveName));
        } else if ("s-maxage".equals(directiveName)) {
            cacheControl.setSMaxAge(readIntValue(reader, directiveName));
        } else {
            String value = null;
            if (reader.hasNextSeparator('=', false)) {
                reader.nextSeparator('=');
                value = reader.nextTokenOrQuotedString().toString();
            }
            cacheControl.getCacheExtension().put(directiveName, value);
        }
    }

    @Override
    public CacheControl fromString(String header) {

        throwIllegalArgumentExceptionIfNull(header, LocalizationMessages.CACHE_CONTROL_IS_NULL());

        try {
            HttpHeaderReader reader = HttpHeaderReader.newInstance(header);
            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoTransform(false); // defaults to true
            while (reader.hasNext()) {
                readDirective(cacheControl, reader);
                if (reader.hasNextSeparator(',', true)) {
                    reader.nextSeparator(',');
                }
            }
            return cacheControl;
        } catch (ParseException pe) {
            throw new IllegalArgumentException(
                    "Error parsing cache control '" + header + "'", pe);
        }
    }

    private void appendWithSeparator(StringBuilder b, String field) {
        if (b.length() > 0) {
            b.append(", ");
        }
        b.append(field);
    }

    private void appendQuotedWithSeparator(StringBuilder b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && !value.isEmpty()) {
            b.append("=\"");
            b.append(value);
            b.append("\"");
        }
    }

    private void appendWithSeparator(StringBuilder b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && !value.isEmpty()) {
            b.append("=");
            b.append(value);
        }
    }

    private void appendWithSeparator(StringBuilder b, String field, int value) {
        appendWithSeparator(b, field);
        b.append("=");
        b.append(value);
    }

    private String buildListValue(List<String> values) {
        StringBuilder b = new StringBuilder();
        for (String value : values) {
            appendWithSeparator(b, value);
        }
        return b.toString();
    }

    private String quoteIfWhitespace(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = WHITESPACE.matcher(value);
        if (m.find()) {
            return "\"" + value + "\"";
        }
        return value;
    }
}
