/*
 * Copyright (c) 2010, 2024 Oracle and/or its affiliates. All rights reserved.
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Helper class for HTTP specified date formats.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public final class HttpDateFormat {

    private static final boolean USE_SIMPLE_DATE_FORMAT_OVER_DATE_TIME_FORMATTER = true;

    /**
     * <p>
     *     A minimum formatter for converting java {@link Date} and {@link LocalDateTime} to {@code String} and vice-versa.
     * </p>
     * <p>
     *     Works as a facade for implementation backed by {@link SimpleDateFormat} and {@link DateTimeFormatter}.
     * </p>
     */
    public static interface HttpDateFormatter {
        /**
         *
         * @param date
         * @return
         */
        Date toDate(String date);

        /**
         *
         * @param date
         * @return
         */
        LocalDateTime toDateTime(String date);
        /**
         * Formats a {@link Date} into a date-time string.
         *
         * @param date the time value to be formatted into a date-time string.
         * @return the formatted date-time string.
         */
        String format(Date date);
        /**
         * Formats a {@link LocalDateTime} into a date-time string.
         *
         * @param dateTime the time value to be formatted into a date-time string.
         * @return the formatted date-time string.
         */
        String format(LocalDateTime dateTime);
    }

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

    private static final List<HttpDateFormatter> dateFormats = createDateFormats();
    private static final Queue<List<HttpDateFormatter>> simpleDateFormats = new ConcurrentLinkedQueue<>();

    private static List<HttpDateFormatter> createDateFormats() {
        final HttpDateFormatter[] formats = new HttpDateFormatter[]{
                new HttpDateFormatterFromDateTimeFormatter(
                        DateTimeFormatter.ofPattern(RFC1123_DATE_FORMAT_PATTERN, Locale.US).withZone(GMT_TIME_ZONE.toZoneId())),
                new HttpDateFormatterFromDateTimeFormatter(
                        DateTimeFormatter.ofPattern(RFC1123_DATE_FORMAT_PATTERN.replace("zzz", "ZZZ"), Locale.US)
                                .withZone(GMT_TIME_ZONE.toZoneId())),
                new HttpDateFormatterFromDateTimeFormatter(
                        DateTimeFormatter.ofPattern(RFC1036_DATE_FORMAT_PATTERN, Locale.US).withZone(GMT_TIME_ZONE.toZoneId())),
                new HttpDateFormatterFromDateTimeFormatter(
                        DateTimeFormatter.ofPattern(RFC1036_DATE_FORMAT_PATTERN.replace("zzz", "ZZZ"), Locale.US)
                                .withZone(GMT_TIME_ZONE.toZoneId())),
                new HttpDateFormatterFromDateTimeFormatter(
                        DateTimeFormatter.ofPattern(ANSI_C_ASCTIME_DATE_FORMAT_PATTERN, Locale.US)
                                .withZone(GMT_TIME_ZONE.toZoneId()))
        };

        return Collections.unmodifiableList(Arrays.asList(formats));
    }

    private static List<HttpDateFormatter> createSimpleDateFormats() {
        final HttpDateFormatterFromSimpleDateTimeFormat[] formats = new HttpDateFormatterFromSimpleDateTimeFormat[]{
                new HttpDateFormatterFromSimpleDateTimeFormat(new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US)),
                new HttpDateFormatterFromSimpleDateTimeFormat(new SimpleDateFormat(RFC1036_DATE_FORMAT_PATTERN, Locale.US)),
                new HttpDateFormatterFromSimpleDateTimeFormat(new SimpleDateFormat(ANSI_C_ASCTIME_DATE_FORMAT_PATTERN, Locale.US))
        };
        formats[0].simpleDateFormat.setTimeZone(GMT_TIME_ZONE);
        formats[1].simpleDateFormat.setTimeZone(GMT_TIME_ZONE);
        formats[2].simpleDateFormat.setTimeZone(GMT_TIME_ZONE);

        return Collections.unmodifiableList(Arrays.asList(formats));
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
    public static HttpDateFormatter getPreferredDateFormatter() {
        if (USE_SIMPLE_DATE_FORMAT_OVER_DATE_TIME_FORMATTER) {
            List<HttpDateFormatter> list = simpleDateFormats.poll();
            if (list == null) {
                list = createSimpleDateFormats();
            }
            // returns clone because calling SDF.parse(...) can change time zone
            final SimpleDateFormat sdf = (SimpleDateFormat)
                    ((HttpDateFormatterFromSimpleDateTimeFormat) list.get(0)).simpleDateFormat.clone();
            simpleDateFormats.add(list);
            return new HttpDateFormatterFromSimpleDateTimeFormat(sdf);
        } else {
            return dateFormats.get(0);
        }
    }

    /**
     * Get the preferred HTTP specified date format (RFC 1123).
     * <p>
     * The date format is scoped to the current thread and may be
     * used without requiring to synchronize access to the instance when
     * parsing or formatting.
     *
     * @return the preferred of data format.
     * @deprecated Use getPreferredDateFormatter instead
     */
    // Unused in Jersey
    @Deprecated(forRemoval = true)
    public static SimpleDateFormat getPreferredDateFormat() {
        List<HttpDateFormatter> list = simpleDateFormats.poll();
        if (list == null) {
            list = createSimpleDateFormats();
        }
        // returns clone because calling SDF.parse(...) can change time zone
        final SimpleDateFormat sdf = (SimpleDateFormat)
                ((HttpDateFormatterFromSimpleDateTimeFormat) list.get(0)).simpleDateFormat.clone();
        simpleDateFormats.add(list);
        return sdf;
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
        return USE_SIMPLE_DATE_FORMAT_OVER_DATE_TIME_FORMATTER
                ? readDateSDF(date)
                : readDateDTF(date);
    }

    private static Date readDateDTF(final String date) throws ParseException {
        final List<HttpDateFormatter> list = dateFormats;
        return readDate(date, list);
    }

    private static Date readDateSDF(final String date) throws ParseException {
        List<HttpDateFormatter> list = simpleDateFormats.poll();
        if (list == null) {
            list = createSimpleDateFormats();
        }
        final Date ret = readDate(date, list);
        simpleDateFormats.add(list);
        return ret;
    }

    private static Date readDate(final String date, List<HttpDateFormatter> formatters) throws ParseException {
        Exception pe = null;
        for (final HttpDateFormatter f : formatters) {
            try {
                return f.toDate(date);
            } catch (final Exception e) {
                pe = (pe == null) ? e : pe;
            }
        }

        throw ParseException.class.isInstance(pe) ? (ParseException) pe
                : new ParseException(pe.getMessage(),
                DateTimeParseException.class.isInstance(pe) ? ((DateTimeParseException) pe).getErrorIndex() : 0);
    }

    /**
     * Warning! DateTimeFormatter is incompatible with SimpleDateFormat for two digits year, since SimpleDateFormat uses
     * 80 years before now and 20 years after, whereas DateTimeFormatter uses years starting with 2000.
     */
    private static class HttpDateFormatterFromDateTimeFormatter implements HttpDateFormatter {
        private final DateTimeFormatter dateTimeFormatter;

        private HttpDateFormatterFromDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
            this.dateTimeFormatter = dateTimeFormatter;
        }

        @Override
        public Date toDate(String date) {
            return new Date(Instant.from(dateTimeFormatter.parse(date)).toEpochMilli());
        }

        @Override
        public LocalDateTime toDateTime(String date) {
            return Instant.from(dateTimeFormatter.parse(date)).atZone(GMT_TIME_ZONE.toZoneId()).toLocalDateTime();
        }

        @Override
        public String format(Date date) {
            return dateTimeFormatter.format(date.toInstant());
        }

        @Override
        public String format(LocalDateTime dateTime) {
            return dateTimeFormatter.format(dateTime);
        }
    }

    private static class HttpDateFormatterFromSimpleDateTimeFormat implements HttpDateFormatter {
        private final SimpleDateFormat simpleDateFormat;

        private HttpDateFormatterFromSimpleDateTimeFormat(SimpleDateFormat simpleDateFormat) {
            this.simpleDateFormat = simpleDateFormat;
        }

        @Override
        public Date toDate(String date) {
            final Date result;
            try {
                result = simpleDateFormat.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            // parse can change time zone -> set it back to GMT
            simpleDateFormat.setTimeZone(GMT_TIME_ZONE);
            return result;
        }

        @Override
        public LocalDateTime toDateTime(String date) {
            return Instant.from(toDate(date).toInstant()).atZone(GMT_TIME_ZONE.toZoneId()).toLocalDateTime();
        }

        @Override
        public String format(Date date) {
            return simpleDateFormat.format(date);
        }

        @Override
        public String format(LocalDateTime dateTime) {
            return simpleDateFormat.format(Date.from(dateTime.atZone(GMT_TIME_ZONE.toZoneId()).toInstant()));
        }
    }
}
