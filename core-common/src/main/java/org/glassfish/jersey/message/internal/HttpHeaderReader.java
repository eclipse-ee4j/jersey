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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import org.glassfish.jersey.internal.util.collection.LRU;

/**
 * An abstract pull-based reader of HTTP headers.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public abstract class HttpHeaderReader {

    /**
     * TODO javadoc.
     */
    public static enum Event {

        Token, QuotedString, Comment, Separator, Control
    }

    /**
     * TODO javadoc.
     */
    public abstract boolean hasNext();

    /**
     * TODO javadoc.
     */
    public abstract boolean hasNextSeparator(char separator, boolean skipWhiteSpace);

    /**
     * TODO javadoc.
     */
    public abstract Event next() throws ParseException;

    /**
     * TODO javadoc.
     */
    public abstract Event next(boolean skipWhiteSpace) throws ParseException;

    /**
     * TODO javadoc.
     */
    protected abstract Event next(boolean skipWhiteSpace, boolean preserveBackslash) throws ParseException;

    /**
     * FIXME remove.
     */
    protected abstract CharSequence nextSeparatedString(char startSeparator, char endSeparator) throws ParseException;

    /**
     * FIXME remove.
     */
    protected abstract Event getEvent();

    /**
     * TODO javadoc.
     */
    public abstract CharSequence getEventValue();

    /**
     * TODO javadoc.
     */
    public abstract CharSequence getRemainder();

    /**
     * TODO javadoc.
     */
    public abstract int getIndex();

    /**
     * TODO javadoc.
     */
    public final CharSequence nextToken() throws ParseException {
        Event e = next(false);
        if (e != Event.Token) {
            throw new ParseException("Next event is not a Token", getIndex());
        }

        return getEventValue();
    }

    /**
     * TODO javadoc.
     */
    public final void nextSeparator(char c) throws ParseException {
        Event e = next(false);
        if (e != Event.Separator) {
            throw new ParseException("Next event is not a Separator", getIndex());
        }

        if (c != getEventValue().charAt(0)) {
            throw new ParseException("Expected separator '" + c + "' instead of '"
                    + getEventValue().charAt(0) + "'", getIndex());
        }
    }

    /**
     * TODO javadoc.
     */
    public final CharSequence nextQuotedString() throws ParseException {
        Event e = next(false);
        if (e != Event.QuotedString) {
            throw new ParseException("Next event is not a Quoted String", getIndex());
        }

        return getEventValue();
    }

    /**
     * TODO javadoc.
     */
    public final CharSequence nextTokenOrQuotedString() throws ParseException {
        return nextTokenOrQuotedString(false);
    }

    private CharSequence nextTokenOrQuotedString(boolean preserveBackslash) throws ParseException {
        Event e = next(false, preserveBackslash);
        if (e != Event.Token && e != Event.QuotedString) {
            throw new ParseException("Next event is not a Token or a Quoted String, "
                    + getEventValue(), getIndex());
        }

        return getEventValue();
    }

    /**
     * TODO javadoc.
     */
    public static HttpHeaderReader newInstance(String header) {
        return new HttpHeaderReaderImpl(header);
    }

    /**
     * TODO javadoc.
     */
    public static HttpHeaderReader newInstance(String header, boolean processComments) {
        return new HttpHeaderReaderImpl(header, processComments);
    }

    /**
     * TODO javadoc.
     */
    public static Date readDate(String date) throws ParseException {
        return HttpDateFormat.readDate(date);
    }

    /**
     * TODO javadoc.
     */
    public static int readQualityFactor(CharSequence q) throws ParseException {
        if (q == null || q.length() == 0) {
            throw new ParseException("Quality value cannot be null or an empty String", 0);
        }

        int index = 0;
        final int length = q.length();
        if (length > 5) {
            throw new ParseException("Quality value is greater than the maximum length, 5", 0);
        }

        // Parse the whole number and decimal point
        final char wholeNumber;
        char c = wholeNumber = q.charAt(index++);
        if (c == '0' || c == '1') {
            if (index == length) {
                return (c - '0') * 1000;
            }
            c = q.charAt(index++);
            if (c != '.') {
                throw new ParseException(
                        "Error parsing Quality value: a decimal place is expected rather than '" + c + "'", index);
            }
            if (index == length) {
                return (c - '0') * 1000;
            }
        } else if (c == '.') {
            // This is not conforming to the HTTP specification but some implementations
            // do this, for example HttpURLConnection.
            if (index == length) {
                throw new ParseException(
                        "Error parsing Quality value: a decimal numeral is expected after the decimal point", index);
            }

        } else {
            throw new ParseException(
                    "Error parsing Quality value: a decimal numeral '0' or '1' is expected rather than '" + c + "'", index);
        }

        // Parse the fraction
        int value = 0;
        int exponent = 100;
        while (index < length) {
            c = q.charAt(index++);
            if (c >= '0' && c <= '9') {
                value += (c - '0') * exponent;
                exponent /= 10;
            } else {
                throw new ParseException(
                        "Error parsing Quality value: a decimal numeral is expected rather than '" + c + "'", index);
            }
        }

        if (wholeNumber == '1') {
            if (value > 0) {
                throw new ParseException("The Quality value, " + q + ", is greater than 1", index);
            }
            return Quality.DEFAULT;
        } else {
            return value;
        }
    }

    /**
     * TODO javadoc.
     */
    public static int readQualityFactorParameter(HttpHeaderReader reader) throws ParseException {
        while (reader.hasNext()) {
            reader.nextSeparator(';');

            // Ignore a ';' with no parameters
            if (!reader.hasNext()) {
                return Quality.DEFAULT;
            }

            // Get the parameter name
            CharSequence name = reader.nextToken();
            reader.nextSeparator('=');
            // Get the parameter value
            CharSequence value = reader.nextTokenOrQuotedString();

            if (name.length() == 1 && (name.charAt(0) == 'q' || name.charAt(0) == 'Q')) {
                return readQualityFactor(value);
            }
        }

        return Quality.DEFAULT;
    }

    /**
     * TODO javadoc.
     */
    public static Map<String, String> readParameters(HttpHeaderReader reader) throws ParseException {
        return readParameters(reader, false);
    }

    /**
     * TODO javadoc.
     */
    public static Map<String, String> readParameters(HttpHeaderReader reader, boolean fileNameFix) throws ParseException {
        Map<String, String> m = null;

        while (reader.hasNext()) {
            reader.nextSeparator(';');
            while (reader.hasNextSeparator(';', true)) {
                reader.next();
            }

            // Ignore a ';' with no parameters
            if (!reader.hasNext()) {
                break;
            }

            // Get the parameter name
            String name = reader.nextToken().toString().toLowerCase(Locale.ROOT);
            reader.nextSeparator('=');
            // Get the parameter value
            String value;
            // fix for http://java.net/jira/browse/JERSEY-759
            if ("filename".equals(name) && fileNameFix) {
                value = reader.nextTokenOrQuotedString(true).toString();
                value = value.substring(value.lastIndexOf('\\') + 1);
            } else {
                value = reader.nextTokenOrQuotedString(false).toString();
            }
            if (m == null) {
                m = new LinkedHashMap<String, String>();
            }

            // Lower case the parameter name
            m.put(name, value);
        }

        return m;
    }

    /**
     * TODO javadoc.
     */
    public static Map<String, Cookie> readCookies(String header) {
        return CookiesParser.parseCookies(header);
    }

    /**
     * TODO javadoc.
     */
    public static Cookie readCookie(String header) {
        return CookiesParser.parseCookie(header);
    }

    /**
     * TODO javadoc.
     */
    public static NewCookie readNewCookie(String header) {
        return CookiesParser.parseNewCookie(header);
    }

    private static interface ListElementCreator<T> {

        T create(HttpHeaderReader reader) throws ParseException;
    }

    private static final ListElementCreator<MatchingEntityTag> MATCHING_ENTITY_TAG_CREATOR =
            new ListElementCreator<MatchingEntityTag>() {

                @Override
                public MatchingEntityTag create(HttpHeaderReader reader) throws ParseException {
                    return MatchingEntityTag.valueOf(reader);
                }
            };

    /**
     * TODO javadoc.
     */
    public static Set<MatchingEntityTag> readMatchingEntityTag(String header) throws ParseException {
        if ("*".equals(header)) {
            return MatchingEntityTag.ANY_MATCH;
        }

        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
        Set<MatchingEntityTag> l = new HashSet<MatchingEntityTag>(1);
        HttpHeaderListAdapter adapter = new HttpHeaderListAdapter(reader);
        while (reader.hasNext()) {
            l.add(MATCHING_ENTITY_TAG_CREATOR.create(adapter));
            adapter.reset();
            if (reader.hasNext()) {
                reader.next();
            }
        }

        return l;
    }

    /**
     * TODO javadoc.
     */
    public static List<MediaType> readMediaTypes(List<MediaType> l, String header) throws ParseException {
        return MEDIA_TYPE_LIST_READER.readList(l, header);
    }

    /**
     * TODO javadoc.
     */
    public static List<AcceptableMediaType> readAcceptMediaType(String header) throws ParseException {
        return ACCEPTABLE_MEDIA_TYPE_LIST_READER.readList(header);
    }

    /**
     * FIXME use somewhere in production code or remove.
     */
    public static List<QualitySourceMediaType> readQualitySourceMediaType(String header) throws ParseException {
        return QUALITY_SOURCE_MEDIA_TYPE_LIST_READER.readList(header);
    }

    /**
     * TODO javadoc.
     */
    public static List<QualitySourceMediaType> readQualitySourceMediaType(String[] header) throws ParseException {
        if (header.length < 2) {
            return readQualitySourceMediaType(header[0]);
        }

        StringBuilder sb = new StringBuilder();
        for (String h : header) {
            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(h);
        }

        return readQualitySourceMediaType(sb.toString());
    }

    /**
     * TODO javadoc.
     */
    public static List<AcceptableMediaType> readAcceptMediaType(
            final String header, final List<QualitySourceMediaType> priorityMediaTypes) throws ParseException {

        return new AcceptMediaTypeListReader(priorityMediaTypes).readList(header);
    }

    /**
     * TODO javadoc.
     */
    public static List<AcceptableToken> readAcceptToken(String header) throws ParseException {
        return ACCEPTABLE_TOKEN_LIST_READER.readList(header);
    }

    /**
     * TODO javadoc.
     */
    public static List<AcceptableLanguageTag> readAcceptLanguage(String header) throws ParseException {
        return ACCEPTABLE_LANGUAGE_TAG_LIST_READER.readList(header);
    }

    /**
     * TODO javadoc.
     */
    public static List<String> readStringList(String header) throws ParseException {
        return STRING_LIST_READER.readList(header);
    }

    private static final MediaTypeListReader MEDIA_TYPE_LIST_READER = new MediaTypeListReader();
    private static final AcceptableMediaTypeListReader ACCEPTABLE_MEDIA_TYPE_LIST_READER = new AcceptableMediaTypeListReader();
    private static final QualitySourceMediaTypeListReader QUALITY_SOURCE_MEDIA_TYPE_LIST_READER =
            new QualitySourceMediaTypeListReader();
    private static final AcceptableTokenListReader ACCEPTABLE_TOKEN_LIST_READER = new AcceptableTokenListReader();
    private static final AcceptableLanguageTagListReader ACCEPTABLE_LANGUAGE_TAG_LIST_READER =
            new AcceptableLanguageTagListReader();
    private static final StringListReader STRING_LIST_READER = new StringListReader();

    private static class MediaTypeListReader extends ListReader<MediaType> {
        private static final ListElementCreator<MediaType> MEDIA_TYPE_CREATOR =
                new ListElementCreator<MediaType>() {

                    @Override
                    public MediaType create(HttpHeaderReader reader) throws ParseException {
                        return MediaTypeProvider.valueOf(reader);
                    }
                };

        List<MediaType> readList(List<MediaType> l, final String header) throws ParseException {
            return super.readList(l, header);
        }

        private MediaTypeListReader() {
            super(MEDIA_TYPE_CREATOR);
        }
    }

    private static class AcceptableMediaTypeListReader extends QualifiedListReader<AcceptableMediaType> {
        private static final ListElementCreator<AcceptableMediaType> ACCEPTABLE_MEDIA_TYPE_CREATOR =
                new ListElementCreator<AcceptableMediaType>() {

                    @Override
                    public AcceptableMediaType create(HttpHeaderReader reader) throws ParseException {
                        return AcceptableMediaType.valueOf(reader);
                    }
                };
        private AcceptableMediaTypeListReader() {
            super(ACCEPTABLE_MEDIA_TYPE_CREATOR, AcceptableMediaType.COMPARATOR);
        }
    }
    /*
     * TODO not used in production?
     */
    private static class QualitySourceMediaTypeListReader extends QualifiedListReader<QualitySourceMediaType> {
        private static final ListElementCreator<QualitySourceMediaType> QUALITY_SOURCE_MEDIA_TYPE_CREATOR =
                new ListElementCreator<QualitySourceMediaType>() {

                    @Override
                    public QualitySourceMediaType create(HttpHeaderReader reader) throws ParseException {
                        return QualitySourceMediaType.valueOf(reader);
                    }
                };
        private QualitySourceMediaTypeListReader() {
            super(QUALITY_SOURCE_MEDIA_TYPE_CREATOR, QualitySourceMediaType.COMPARATOR);
        }
    }

    /*
     * TODO this is used in tests only
     */
    private static class AcceptMediaTypeListReader extends QualifiedListReader<AcceptableMediaType> {
        AcceptMediaTypeListReader(List<QualitySourceMediaType> priorityMediaTypes) {
            super(ACCEPTABLE_MEDIA_TYPE_CREATOR, new AcceptableMediaTypeComparator(priorityMediaTypes));
        }

        private static final ListElementCreator<AcceptableMediaType> ACCEPTABLE_MEDIA_TYPE_CREATOR =
                new ListElementCreator<AcceptableMediaType>() {

                    @Override
                    public AcceptableMediaType create(HttpHeaderReader reader) throws ParseException {
                        return AcceptableMediaType.valueOf(reader);
                    }
                };

        private static class AcceptableMediaTypeComparator implements Comparator<AcceptableMediaType> {
            private final List<QualitySourceMediaType> priorityMediaTypes;

            private AcceptableMediaTypeComparator(List<QualitySourceMediaType> priorityMediaTypes) {
                this.priorityMediaTypes = priorityMediaTypes;
            }

            @Override
            public int compare(AcceptableMediaType o1, AcceptableMediaType o2) {
                // FIXME what is going on here?
                boolean q_o1_set = false;
                int q_o1 = 0;
                boolean q_o2_set = false;
                int q_o2 = 0;
                for (QualitySourceMediaType priorityType : priorityMediaTypes) {
                    if (!q_o1_set && MediaTypes.typeEqual(o1, priorityType)) {
                        q_o1 = o1.getQuality() * priorityType.getQuality();
                        q_o1_set = true;
                    } else if (!q_o2_set && MediaTypes.typeEqual(o2, priorityType)) {
                        q_o2 = o2.getQuality() * priorityType.getQuality();
                        q_o2_set = true;
                    }
                }
                int i = q_o2 - q_o1;
                if (i != 0) {
                    return i;
                }

                i = o2.getQuality() - o1.getQuality();
                if (i != 0) {
                    return i;
                }

                return MediaTypes.PARTIAL_ORDER_COMPARATOR.compare(o1, o2);
            }
        };


    }

    private static class AcceptableTokenListReader extends QualifiedListReader<AcceptableToken> {
        private static final ListElementCreator<AcceptableToken> ACCEPTABLE_TOKEN_CREATOR =
                new ListElementCreator<AcceptableToken>() {

                    @Override
                    public AcceptableToken create(HttpHeaderReader reader) throws ParseException {
                        return new AcceptableToken(reader);
                    }
                };
        private AcceptableTokenListReader() {
            super(ACCEPTABLE_TOKEN_CREATOR);
        }
    }

    private static class AcceptableLanguageTagListReader extends QualifiedListReader<AcceptableLanguageTag> {
        private static final ListElementCreator<AcceptableLanguageTag> LANGUAGE_CREATOR =
                new ListElementCreator<AcceptableLanguageTag>() {

                    @Override
                    public AcceptableLanguageTag create(HttpHeaderReader reader) throws ParseException {
                        return new AcceptableLanguageTag(reader);
                    }
                };
        private AcceptableLanguageTagListReader() {
            super(LANGUAGE_CREATOR);
        }
    }

    private abstract static class QualifiedListReader<T extends Qualified> extends ListReader<T> {
        @Override
        public List<T> readList(String header) throws ParseException {
            List<T> l = super.readList(header);
            Collections.sort(l, comparator);
            return l;
        }

        private final Comparator<T> comparator;
        private QualifiedListReader(ListElementCreator<T> creator) {
            this(creator, (Comparator<T>) Quality.QUALIFIED_COMPARATOR);
        }

        protected QualifiedListReader(ListElementCreator<T> creator, Comparator<T> comparator) {
            super(creator);
            this.comparator = comparator;
        }
    }

    private static class StringListReader extends ListReader<String> {
        private static final ListElementCreator<String> listElementCreator = new ListElementCreator<String>() {
            @Override
            public String create(HttpHeaderReader reader) throws ParseException {
                reader.hasNext();
                return reader.nextToken().toString();
            }
        };

        private StringListReader() {
            super(listElementCreator);
        }
    }

    private abstract static class ListReader<T> {
        private final LRU<String, List<T>> LIST_CACHE = LRU.create();
        private final Lock lock = new ReentrantLock();
        protected final ListElementCreator<T> creator;

        protected ListReader(ListElementCreator<T> creator) {
            this.creator = creator;
        }

        protected List<T> readList(final String header) throws ParseException {
            return readList(new ArrayList<T>(), header);
        }

        private List<T> readList(final List<T> l, final String header)
                throws ParseException {

//            List<T> list = null;
            List<T> list = LIST_CACHE.getIfPresent(header);

            if (list == null) {
                lock.lock();
                try {
                    list = LIST_CACHE.getIfPresent(header);
                    if (list == null) {
                        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
                        HttpHeaderListAdapter adapter = new HttpHeaderListAdapter(reader);
                        list = new LinkedList<>();

                        while (reader.hasNext()) {
                            list.add(creator.create(adapter));
                            adapter.reset();
                            if (reader.hasNext()) {
                                reader.next();
                            }
                        }
                        LIST_CACHE.put(header, list);
                    }
                } finally {
                    lock.unlock();
                }
            }

            l.addAll(list);
            return l;
        }
    }
}
