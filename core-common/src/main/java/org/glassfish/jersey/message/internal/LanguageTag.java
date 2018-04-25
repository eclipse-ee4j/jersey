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

/**
 * A language tag.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class LanguageTag {

    String tag;
    String primaryTag;
    String subTags;

    protected LanguageTag() {
    }

    public static LanguageTag valueOf(final String s) throws IllegalArgumentException {
        final LanguageTag lt = new LanguageTag();

        try {
            lt.parse(s);
        } catch (final ParseException pe) {
            throw new IllegalArgumentException(pe);
        }

        return lt;
    }

    public LanguageTag(final String primaryTag, final String subTags) {
        if (subTags != null && subTags.length() > 0) {
            this.tag = primaryTag + "-" + subTags;
        } else {
            this.tag = primaryTag;
        }

        this.primaryTag = primaryTag;

        this.subTags = subTags;
    }

    public LanguageTag(final String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    public LanguageTag(final HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        tag = reader.nextToken().toString();

        if (reader.hasNext()) {
            throw new ParseException("Invalid Language tag", reader.getIndex());
        }

        parse(tag);
    }

    public final boolean isCompatible(final Locale tag) {
        if (this.tag.equals("*")) {
            return true;
        }

        if (subTags == null) {
            return primaryTag.equalsIgnoreCase(tag.getLanguage());
        } else {
            return primaryTag.equalsIgnoreCase(tag.getLanguage())
                    && subTags.equalsIgnoreCase(tag.getCountry());
        }
    }

    public final Locale getAsLocale() {
        return (subTags == null)
                ? new Locale(primaryTag)
                : new Locale(primaryTag, subTags);
    }

    protected final void parse(final String languageTag) throws ParseException {
        if (!isValid(languageTag)) {
            throw new ParseException("String, " + languageTag + ", is not a valid language tag", 0);
        }

        final int index = languageTag.indexOf('-');
        if (index == -1) {
            primaryTag = languageTag;
            subTags = null;
        } else {
            primaryTag = languageTag.substring(0, index);
            subTags = languageTag.substring(index + 1, languageTag.length());
        }
    }

    /**
     * Validate input tag (header value) according to HTTP 1.1 spec + allow region code (numeric) instead of country code.
     *
     * @param tag accept-language header value.
     * @return {@code true} if the given value is valid language tag, {@code false} instead.
     */
    private boolean isValid(final String tag) {
        int alphanumCount = 0;
        int dash = 0;
        for (int i = 0; i < tag.length(); i++) {
            final char c = tag.charAt(i);
            if (c == '-') {
                if (alphanumCount == 0) {
                    return false;
                }
                alphanumCount = 0;
                dash++;
            } else if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || (dash > 0 && '0' <= c && c <= '9')) {
                alphanumCount++;
                if (alphanumCount > 8) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return (alphanumCount != 0);
    }

    public final String getTag() {
        return tag;
    }

    public final String getPrimaryTag() {
        return primaryTag;
    }

    public final String getSubTags() {
        return subTags;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LanguageTag) || o.getClass() != this.getClass()) {
            return false;
        }

        final LanguageTag that = (LanguageTag) o;

        if (primaryTag != null ? !primaryTag.equals(that.primaryTag) : that.primaryTag != null) {
            return false;
        }
        if (subTags != null ? !subTags.equals(that.subTags) : that.subTags != null) {
            return false;
        }
        return !(tag != null ? !tag.equals(that.tag) : that.tag != null);

    }

    @Override
    public int hashCode() {
        int result = tag != null ? tag.hashCode() : 0;
        result = 31 * result + (primaryTag != null ? primaryTag.hashCode() : 0);
        result = 31 * result + (subTags != null ? subTags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return primaryTag + (subTags == null ? "" : subTags);
    }
}
