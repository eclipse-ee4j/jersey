/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.jersey.message.internal.HttpDateFormat;
import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.uri.UriComponent;

/**
 * A content disposition header.
 *
 * @author Paul Sandoz
 * @author imran@smartitengineering.com
 * @author Marek Potociar
 */
public class ContentDisposition {

    private final CharSequence type;
    private final Map<String, String> parameters;
    private String fileName;
    private Date creationDate;
    private Date modificationDate;
    private Date readDate;
    private long size;

    private static final String CHARSET_GROUP_NAME = "charset";
    private static final String CHARSET_REGEX = "(?<" + CHARSET_GROUP_NAME + ">[^']+)";
    private static final String LANG_GROUP_NAME = "lang";
    private static final String LANG_REGEX = "(?<" + LANG_GROUP_NAME + ">[a-z]{2,8}(-[a-z0-9-]+)?)?";
    private static final String FILENAME_GROUP_NAME = "filename";
    private static final String FILENAME_REGEX = "(?<" + FILENAME_GROUP_NAME + ">.+)";
    private static final Pattern FILENAME_EXT_VALUE_PATTERN =
            Pattern.compile(CHARSET_REGEX + "'" + LANG_REGEX + "'" + FILENAME_REGEX,
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern FILENAME_VALUE_CHARS_PATTERN =
            Pattern.compile("(%[a-f0-9]{2}|[a-z0-9!#$&+.^_`|~-])+", Pattern.CASE_INSENSITIVE);

    protected ContentDisposition(final String type, final String fileName, final Date creationDate,
                                 final Date modificationDate, final Date readDate, final long size) {
        this.type = type;
        this.fileName = fileName;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.readDate = readDate;
        this.size = size;
        this.parameters = Collections.emptyMap();
    }

    public ContentDisposition(final String header) throws ParseException {
        this(header, false);
    }

    public ContentDisposition(final String header, final boolean fileNameFix) throws ParseException {
        this(HttpHeaderReader.newInstance(header), fileNameFix);
    }

    public ContentDisposition(final HttpHeaderReader reader, final boolean fileNameFix) throws ParseException {
        reader.hasNext();

        type = reader.nextToken();

        final Map<String, String> paramsOrNull = reader.hasNext()
                ? HttpHeaderReader.readParameters(reader, fileNameFix)
                : null;

        parameters = paramsOrNull == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(paramsOrNull);

        createParameters();
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public String getType() {
        return (type == null) ? null : type.toString();
    }

    /**
     * Get the parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Get the filename parameter.
     *
     * @return the size
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the creation-date parameter.
     *
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Get the modification-date parameter.
     *
     * @return the modificationDate
     */
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * Get the read-date parameter.
     *
     * @return the readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Get the size parameter.
     *
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Convert the disposition to a "Content-Disposition" header value.
     *
     * @return the "Content-Disposition" value.
     */
    @Override
    public String toString() {
        return toStringBuffer().toString();
    }

    protected StringBuilder toStringBuffer() {
        final StringBuilder sb = new StringBuilder();

        sb.append(type);
        addStringParameter(sb, "filename", fileName);
        addDateParameter(sb, "creation-date", creationDate);
        addDateParameter(sb, "modification-date", modificationDate);
        addDateParameter(sb, "read-date", readDate);
        addLongParameter(sb, "size", size);

        return sb;
    }

    protected void addStringParameter(final StringBuilder sb, final String name, final String p) {
        if (p != null) {
            sb.append("; ").append(name).append("=\"").append(p).append("\"");
        }
    }

    protected void addDateParameter(final StringBuilder sb, final String name, final Date p) {
        if (p != null) {
            sb.append("; ").append(name).append("=\"").append(HttpDateFormat.getPreferredDateFormat().format(p)).append("\"");
        }
    }

    protected void addLongParameter(final StringBuilder sb, final String name, final Long p) {
        if (p != -1) {
            sb.append("; ").append(name).append('=').append(Long.toString(p));
        }
    }

    private void createParameters() throws ParseException {
        fileName = defineFileName();

        creationDate = createDate("creation-date");

        modificationDate = createDate("modification-date");

        readDate = createDate("read-date");

        size = createLong("size");
    }

    private String defineFileName() throws ParseException {

        final String fileName = parameters.get("filename");
        final String fileNameExt = parameters.get("filename*");

        if (fileNameExt == null) {
            return fileName;
        }

        final Matcher matcher = FILENAME_EXT_VALUE_PATTERN.matcher(fileNameExt);

        if (matcher.matches()) {

            final String fileNameValueChars = matcher.group(FILENAME_GROUP_NAME);
            if (isFilenameValueCharsEncoded(fileNameValueChars)) {
                return fileNameExt;
            }

            final String charset = matcher.group(CHARSET_GROUP_NAME);
            if (matcher.group(CHARSET_GROUP_NAME).equalsIgnoreCase("UTF-8")) {
                final String language = matcher.group(LANG_GROUP_NAME);
                return new StringBuilder(charset)
                        .append("'")
                        .append(language == null ? "" : language)
                        .append("'")
                        .append(encodeToUriFormat(fileNameValueChars))
                        .toString();
            } else {
                throw new ParseException(charset + " charset is not supported", 0);
            }
        }

        throw new ParseException(fileNameExt + " - unsupported filename parameter", 0);
    }

    private String encodeToUriFormat(final String parameter) {
        return UriComponent.contextualEncode(parameter, UriComponent.Type.UNRESERVED);
    }

    private boolean isFilenameValueCharsEncoded(final String parameter) {
        return FILENAME_VALUE_CHARS_PATTERN.matcher(parameter).matches();
    }

    private Date createDate(final String name) throws ParseException {
        final String value = parameters.get(name);
        if (value == null) {
            return null;
        }
        return HttpDateFormat.getPreferredDateFormat().parse(value);
    }

    private long createLong(final String name) throws ParseException {
        final String value = parameters.get(name);
        if (value == null) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
            throw new ParseException("Error parsing size parameter of value, " + value, 0);
        }
    }

    /**
     * Start building content disposition.
     *
     * @param type the disposition type.
     * @return the content disposition builder.
     */
    public static ContentDispositionBuilder type(final String type) {
        return new ContentDispositionBuilder(type);
    }

    /**
     * Builder to build content disposition.
     *
     * @param <T> the builder type.
     * @param <V> the content disposition type.
     */
    public static class ContentDispositionBuilder<T extends ContentDispositionBuilder, V extends ContentDisposition> {

        protected String type;
        protected String fileName;
        protected Date creationDate;
        protected Date modificationDate;
        protected Date readDate;
        protected long size = -1;

        ContentDispositionBuilder(final String type) {
            this.type = type;
        }

        /**
         * Add the "file-name" parameter.
         *
         * @param fileName the "file-name" parameter. If null the value
         *        is removed
         * @return this builder.
         */
        @SuppressWarnings("unchecked")
        public T fileName(final String fileName) {
            this.fileName = fileName;
            return (T) this;
        }

        /**
         * Add the "creation-date" parameter.
         *
         * @param creationDate the "creation-date" parameter. If null the value
         *        is removed
         * @return this builder.
         */
        @SuppressWarnings("unchecked")
        public T creationDate(final Date creationDate) {
            this.creationDate = creationDate;
            return (T) this;
        }

        /**
         * Add the "modification-date" parameter.
         *
         * @param modificationDate the "modification-date" parameter. If null the value
         *        is removed
         * @return this builder.
         */
        @SuppressWarnings("unchecked")
        public T modificationDate(final Date modificationDate) {
            this.modificationDate = modificationDate;
            return (T) this;
        }

        /**
         * Add the "read-date" parameter.
         *
         * @param readDate the "read-date" parameter. If null the value
         *        is removed
         * @return this builder.
         */
        @SuppressWarnings("unchecked")
        public T readDate(final Date readDate) {
            this.readDate = readDate;
            return (T) this;
        }

        /**
         * Add the "size" parameter.
         *
         * @param size the "size" parameter. If -1 the value is removed.
         * @return this builder.
         */
        @SuppressWarnings("unchecked")
        public T size(final long size) {
            this.size = size;
            return (T) this;
        }

        /**
         * Build the content disposition.
         *
         * @return the content disposition.
         */
        @SuppressWarnings("unchecked")
        public V build() {
            final ContentDisposition cd = new ContentDisposition(type, fileName, creationDate, modificationDate, readDate, size);
            return (V) cd;
        }
    }
}
