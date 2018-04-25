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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class for HTTP specified date formats.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class HttpDateFormat {

    private HttpDateFormat() {
    }
    /**
     * The date format pattern for RFC 1123.
     */
    private static final String RFC1123_DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    /**
     * The date format pattern for RFC 1036.
     */
    private static final String RFC1036_DATE_FORMAT_PATTERN = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    /**
     * The date format pattern for ANSI C asctime().
     */
    private static final String ANSI_C_ASCTIME_DATE_FORMAT_PATTERN = "EEE MMM d HH:mm:ss yyyy";

    private static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    private static final ThreadLocal<List<SimpleDateFormat>> dateFormats = new ThreadLocal<List<SimpleDateFormat>>() {

        @Override
        protected synchronized List<SimpleDateFormat> initialValue() {
            return createDateFormats();
        }
    };

    private static List<SimpleDateFormat> createDateFormats() {
        final SimpleDateFormat[] formats = new SimpleDateFormat[]{
            new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(RFC1036_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(ANSI_C_ASCTIME_DATE_FORMAT_PATTERN, Locale.US)
        };
        formats[0].setTimeZone(GMT_TIME_ZONE);
        formats[1].setTimeZone(GMT_TIME_ZONE);
        formats[2].setTimeZone(GMT_TIME_ZONE);

        return Collections.unmodifiableList(Arrays.asList(formats));
    }

    /**
     * Return an unmodifiable list of HTTP specified date formats to use for
     * parsing or formatting {@link Date}.
     * <p>
     * The list of date formats are scoped to the current thread and may be
     * used without requiring to synchronize access to the instances when
     * parsing or formatting.
     *
     * @return the list of data formats.
     */
    private static List<SimpleDateFormat> getDateFormats() {
        return dateFormats.get();
    }

    /**
     * Get the preferred HTTP specified date format (RFC 1123).
     * <p>
     * The date format is scoped to the current thread and may be
     * used without requiring to synchronize access to the instance when
     * parsing or formatting.
     *
     * @return the preferred of data format.
     */
    public static SimpleDateFormat getPreferredDateFormat() {
        // returns clone because calling SDF.parse(...) can change time zone
        return (SimpleDateFormat) dateFormats.get().get(0).clone();
    }

    /**
     * Read a date.
     *
     * @param date the date as a string.
     *
     * @return the date
     * @throws java.text.ParseException in case the date string cannot be parsed.
     */
    public static Date readDate(final String date) throws ParseException {
        ParseException pe = null;
        for (final SimpleDateFormat f : HttpDateFormat.getDateFormats()) {
            try {
                Date result = f.parse(date);
                // parse can change time zone -> set it back to GMT
                f.setTimeZone(GMT_TIME_ZONE);
                return result;
            } catch (final ParseException e) {
                pe = (pe == null) ? e : pe;
            }
        }

        throw pe;
    }
}
