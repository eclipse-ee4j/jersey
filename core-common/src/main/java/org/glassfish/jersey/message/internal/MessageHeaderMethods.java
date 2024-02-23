/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.RuntimeDelegateDecorator;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Common header methods for outbound and inbound messages.
 */
public abstract class MessageHeaderMethods {
    protected RuntimeDelegate runtimeDelegateDecorator;

    protected MessageHeaderMethods(Configuration configuration) {
        this.runtimeDelegateDecorator = RuntimeDelegateDecorator.configured(configuration);
    }

    protected MessageHeaderMethods(MessageHeaderMethods other) {
        this.runtimeDelegateDecorator = other.runtimeDelegateDecorator;
    }

    /**
     * Get a message header as a single string value.
     *
     * Each single non-string header value is converted to String using a {@code RuntimeDelegate.HeaderDelegate} if one
     * is available via {@code RuntimeDelegate#createHeaderDelegate(java.lang.Class)} for the header value
     * class or using its {@code toString} method if a header delegate is not available.
     *
     * @param name the message header.
     * @return the message header value. If the message header is not present then {@code null} is returned. If the message
     * header is present but has no value then the empty string is returned. If the message header is present more than once
     * then the values of joined together and separated by a ',' character.
     */
    public abstract String getHeaderString(String name);

    /**
     * Get the mutable message headers multivalued map.
     *
     * @return mutable multivalued map of message headers.
     */
    public abstract MultivaluedMap<String, ?> getHeaders();

    /**
     * Return {@link HeaderValueException.Context} type of the message context.
     * @return {@link HeaderValueException.Context} type of the message context.
     */
    protected abstract HeaderValueException.Context getHeaderValueExceptionContext();

    /**
     * Get the links attached to the message as header.
     *
     * @return links, may return empty {@link java.util.Set} if no links are present. Never
     * returns {@code null}.
     */
    public abstract Set<Link> getLinks();

    /**
     * Checks whether a header with a specific name and value (or item of the token-separated value list) exists.
     *
     * Each single non-string header value is converted to String using a {@code RuntimeDelegate.HeaderDelegate} if one
     * is available via {@code RuntimeDelegate#createHeaderDelegate(java.lang.Class)} for the header value
     * class or using its {@code toString} method if a header delegate is not available.
     *
     * <p>
     * For example: {@code containsHeaderString("cache-control", ",", "no-store"::equalsIgnoreCase)} will return {@code true} if
     * a {@code Cache-Control} header exists that has the value {@code no-store}, the value {@code No-Store} or the value
     * {@code Max-Age, NO-STORE, no-transform}, but {@code false} when it has the value {@code no-store;no-transform}
     * (missing comma), or the value {@code no - store} (whitespace within value).
     *
     * @param name the message header.
     * @param valueSeparatorRegex Separates the header value into single values. {@code null} does not split.
     * @param valuePredicate value must fulfil this predicate.
     * @return {@code true} if and only if a header with the given name exists, having either a whitespace-trimmed value
     * matching the predicate, or having at least one whitespace-trimmed single value in a token-separated list of single values.
     */
    public boolean containsHeaderString(String name, String valueSeparatorRegex, Predicate<String> valuePredicate) {
        final String header = getHeaderString(name);
        if (header == null) {
            return false;
        }
        final String[] split = header.split(valueSeparatorRegex);
        for (String s : split) {
            if (valuePredicate.test(s.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a header with a specific name and value (or item of the comma-separated value list) exists.
     *
     * Each single non-string header value is converted to String using a {@code RuntimeDelegate.HeaderDelegate} if one
     * is available via {@code RuntimeDelegate#createHeaderDelegate(java.lang.Class)} for the header value
     * class or using its {@code toString} method if a header delegate is not available.
     *
     * <p>
     * For example: {@code containsHeaderString("cache-control", "no-store"::equalsIgnoreCase)} will return {@code true} if
     * a {@code Cache-Control} header exists that has the value {@code no-store}, the value {@code No-Store} or the value
     * {@code Max-Age, NO-STORE, no-transform}, but {@code false} when it has the value {@code no-store;no-transform}
     * (missing comma), or the value {@code no - store} (whitespace within value).
     *
     * @param name the message header.
     * @param valuePredicate value must fulfil this predicate.
     * @return {@code true} if and only if a header with the given name exists, having either a whitespace-trimmed value
     * matching the predicate, or having at least one whitespace-trimmed single value in a comma-separated list of single values.
     */
    public boolean containsHeaderString(String name, Predicate<String> valuePredicate) {
        return containsHeaderString(name, ",", valuePredicate);
    }

    /**
     * Get the allowed HTTP methods from the Allow HTTP header.
     *
     * @return the allowed HTTP methods, all methods will returned as upper case
     * strings.
     */
    public Set<String> getAllowedMethods() {
        final String allowed = getHeaderString(HttpHeaders.ALLOW);
        if (allowed == null || allowed.isEmpty()) {
            return Collections.emptySet();
        }
        try {
            return new HashSet<String>(HttpHeaderReader.readStringList(allowed.toUpperCase(Locale.ROOT)));
        } catch (java.text.ParseException e) {
            throw exception(HttpHeaders.ALLOW, allowed, e);
        }
    }

    /**
     * Get message date.
     *
     * @return the message date, otherwise {@code null} if not present.
     */
    public Date getDate() {
        return singleHeader(HttpHeaders.DATE, Date.class, input -> {
            try {
                return HttpHeaderReader.readDate(input);
            } catch (ParseException e) {
                throw new ProcessingException(e);
            }
        }, false);
    }

    /**
     * Get the entity tag.
     *
     * @return the entity tag, otherwise {@code null} if not present.
     */
    public EntityTag getEntityTag() {
        return singleHeader(HttpHeaders.ETAG, EntityTag.class, new Function<String, EntityTag>() {
            @Override
            public EntityTag apply(String value) {
                try {
                    return value == null ? null : EntityTag.valueOf(value);
                } catch (IllegalArgumentException ex) {
                    throw new ProcessingException(ex);
                }
            }
        }, false);
    }

    /**
     * Get the language of the entity.
     *
     * @return the language of the entity or {@code null} if not specified
     */
    public Locale getLanguage() {
        return singleHeader(HttpHeaders.CONTENT_LANGUAGE, Locale.class, input -> {
            try {
                return new LanguageTag(input).getAsLocale();
            } catch (ParseException e) {
                throw new ProcessingException(e);
            }
        }, false);
    }

    /**
     * Get the last modified date.
     *
     * @return the last modified date, otherwise {@code null} if not present.
     */
    public Date getLastModified() {
        return singleHeader(HttpHeaders.LAST_MODIFIED, Date.class, new Function<String, Date>() {
            @Override
            public Date apply(String input) {
                try {
                    return HttpHeaderReader.readDate(input);
                } catch (ParseException e) {
                    throw new ProcessingException(e);
                }
            }
        }, false);
    }

    /**
     * Get Content-Length value.
     * <p>
     * <B>Note</B>: {@link #getLengthLong() getLengthLong()}
     * should be preferred over this method, since it returns a {@code long}
     * instead and is therefore more portable.</P>
     *
     * @return Content-Length as a postive integer if present and valid number, {@code -1} if negative number.
     * @throws ProcessingException when {@link Integer#parseInt(String)} (String)} throws {@link NumberFormatException}.
     */
    public int getLength() {
        return singleHeader(HttpHeaders.CONTENT_LENGTH, Integer.class, input -> {
            try {
                if (input != null && !input.isEmpty()) {
                    int i = Integer.parseInt(input);
                    if (i >= 0) {
                        return i;
                    }
                }
                return -1;

            } catch (NumberFormatException ex) {
                throw new ProcessingException(ex);
            }
        }, true);
    }

    /**
     * Get Content-Length value.
     *
     * @return Content-Length as a positive long if present and valid number, {@code -1} if negative number.
     * @throws ProcessingException when {@link Long#parseLong(String)} throws {@link NumberFormatException}.
     */
    public long getLengthLong() {
        return singleHeader(HttpHeaders.CONTENT_LENGTH, Long.class, input -> {
            try {
                if (input != null && !input.isEmpty()) {
                    long l = Long.parseLong(input);
                    if (l >= 0) {
                        return l;
                    }
                }
                return -1L;
            } catch (NumberFormatException ex) {
                throw new ProcessingException(ex);
            }
        }, true);
    }

    /**
     * Get the link for the relation.
     *
     * @param relation link relation.
     * @return the link for the relation, otherwise {@code null} if not present.
     */
    public Link getLink(String relation) {
        for (Link link : getLinks()) {
            List<String> relations = LinkProvider.getLinkRelations(link.getRel());
            if (relations != null && relations.contains(relation)) {
                return link;
            }
        }
        return null;
    }

    /**
     * Convenience method that returns a {@link jakarta.ws.rs.core.Link.Builder Link.Builder}
     * for the relation.
     *
     * @param relation link relation.
     * @return the link builder for the relation, otherwise {@code null} if not
     * present.
     */
    public Link.Builder getLinkBuilder(String relation) {
        Link link = getLink(relation);
        if (link == null) {
            return null;
        }

        return Link.fromLink(link);
    }

    /**
     * Get the location.
     *
     * @return the location URI, otherwise {@code null} if not present.
     */
    public URI getLocation() {
        return singleHeader(HttpHeaders.LOCATION, URI.class, value -> {
            try {
                return value == null ? null : URI.create(value);
            } catch (IllegalArgumentException ex) {
                throw new ProcessingException(ex);
            }
        }, false);
    }

    /**
     * Get any cookies that accompanied the message.
     *
     * @return a read-only map of cookie name (String) to {@link jakarta.ws.rs.core.Cookie}.
     */
    public Map<String, Cookie> getRequestCookies() {
        @SuppressWarnings("unchecked")
        final List<Object> cookies = (List<Object>) getHeaders().get(HttpHeaders.COOKIE);
        if (cookies == null || cookies.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Cookie> result = new HashMap<String, Cookie>();
        for (String cookie : toStringList(cookies)) {
            if (cookie != null) {
                result.putAll(HttpHeaderReader.readCookies(cookie));
            }
        }
        return result;
    }

    /**
     * Get any new cookies set on the message.
     *
     * @return a read-only map of cookie name (String) to a {@link jakarta.ws.rs.core.NewCookie new cookie}.
     */
    public Map<String, NewCookie> getResponseCookies() {
        @SuppressWarnings("unchecked")
        List<Object> cookies = (List<Object>) getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null || cookies.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, NewCookie> result = new HashMap<String, NewCookie>();
        for (String cookie : toStringList(cookies)) {
            if (cookie != null) {
                NewCookie newCookie = HttpHeaderReader.readNewCookie(cookie);
                String cookieName = newCookie.getName();
                if (result.containsKey(cookieName)) {
                    result.put(cookieName, HeaderUtils.getPreferredCookie(result.get(cookieName), newCookie));
                } else {
                    result.put(cookieName, newCookie);
                }
            }
        }
        return result;
    }

    /**
     * Check if link for relation exists.
     *
     * @param relation link relation.
     * @return {@code true} if the for the relation link exists, {@code false}
     * otherwise.
     */
    public boolean hasLink(String relation) {
        for (Link link : getLinks()) {
            List<String> relations = LinkProvider.getLinkRelations(link.getRel());

            if (relations != null && relations.contains(relation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a single typed header value.
     *
     * @param <T>         header value type.
     * @param name        header name.
     * @param valueType   header value class.
     * @param converter   from string conversion function. Is expected to throw {@link ProcessingException}
     *                    if conversion fails.
     * @param convertNull if {@code true} this method calls the provided converter even for {@code null}. Otherwise this
     *                    method returns the {@code null} without calling the converter.
     * @return value of the header, or (possibly converted) {@code null} if not present.
     */
    protected <T> T singleHeader(String name, Class<T> valueType, Function<String, T> converter, boolean convertNull) {
        @SuppressWarnings("unchecked")
        final List<Object> values = (List<Object>) getHeaders().get(name);

        if (values == null || values.isEmpty()) {
            return convertNull ? converter.apply(null) : null;
        }
        if (values.size() > 1) {
            throw new HeaderValueException(
                    LocalizationMessages.TOO_MANY_HEADER_VALUES(name, values.toString()),
                    getHeaderValueExceptionContext());
        }

        Object value = values.get(0);
        if (value == null) {
            return convertNull ? converter.apply(null) : null;
        }

        if (HeaderValueException.Context.OUTBOUND == getHeaderValueExceptionContext() && valueType.isInstance(value)) {
            return valueType.cast(value);
        } else {
            try {
                return converter.apply(HeaderUtils.asString(value, runtimeDelegateDecorator));
            } catch (ProcessingException ex) {
                throw exception(name, value, ex);
            }
        }
    }

    /**
     * Get a single typed header value for Inbound messages
     *
     * @param <T>         header value type.
     * @param name        header name.
     * @param converter   from string conversion function. Is expected to throw {@link ProcessingException}
     *                    if conversion fails.
     * @param convertNull if {@code true} this method calls the provided converter even for {@code null}. Otherwise this
     *                    method returns the {@code null} without calling the converter.
     * @return value of the header, or (possibly converted) {@code null} if not present.
     */
    protected <T> T singleHeader(String name, Function<String, T> converter, boolean convertNull) {
        return singleHeader(name, null, converter, convertNull);
    }

    protected HeaderValueException exception(final String headerName, Object headerValue, Exception e) {
        return new HeaderValueException(LocalizationMessages.UNABLE_TO_PARSE_HEADER_VALUE(headerName, headerValue), e,
                getHeaderValueExceptionContext());
    }

    private List<String> toStringList(List list) {
        return getHeaderValueExceptionContext() == HeaderValueException.Context.OUTBOUND
                ? HeaderUtils.asStringList(list, runtimeDelegateDecorator)
                : (List<String>) list;
    }
}
