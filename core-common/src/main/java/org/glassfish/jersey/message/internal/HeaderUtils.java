/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.AbstractMultivaluedMap;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.glassfish.jersey.internal.RuntimeDelegateDecorator;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.glassfish.jersey.internal.util.collection.Views;

/**
 * Utility class supporting the processing of message headers.
 *
 * @author Marek Potociar
 * @author Michal Gajdos
 * @author Libor Kramolis
 */
public final class HeaderUtils {

    private static final Logger LOGGER = Logger.getLogger(HeaderUtils.class.getName());

    /**
     * Create an empty inbound message headers container. Created container is mutable.
     *
     * @return a new empty mutable container for storing inbound message headers.
     */
    public static AbstractMultivaluedMap<String, String> createInbound() {
        return new StringKeyIgnoreCaseMultivaluedMap<String>();
    }

    /**
     * Get immutable empty message headers container. The factory method can be
     * used to for both message header container types&nbsp;&nbsp;&ndash;&nbsp;&nbsp;inbound
     * as well as outbound.
     *
     * @param <V> header value type. Typically {@link Object} in case of the outbound
     *            headers and {@link String} in case of the inbound headers.
     * @return an immutable empty message headers container.
     */
    public static <V> MultivaluedMap<String, V> empty() {
        return ImmutableMultivaluedMap.empty();
    }

    /**
     * Create an empty outbound message headers container. Created container is mutable.
     *
     * @return a new empty mutable container for storing outbound message headers.
     */
    public static AbstractMultivaluedMap<String, Object> createOutbound() {
        return new StringKeyIgnoreCaseMultivaluedMap<Object>();
    }

    /**
     * Convert a message header value, represented as a general object, to it's
     * string representation. If the supplied header value is {@code null},
     * this method returns {@code null}.
     * <p>
     * This method defers to {@link RuntimeDelegate#createHeaderDelegate} to
     * obtain a {@link HeaderDelegate} or {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     * to convert the value to a {@code String}.
     * If neither is found then the {@code toString()}
     * method on the header object is utilized.
     *
     * @param headerValue the header value represented as an object.
     * @param rd          runtime delegate instance to be used for header delegate
     *                    retrieval. If {@code null}, a default {@code RuntimeDelegate}
     *                    instance will be {@link RuntimeDelegate#getInstance() obtained} and
     *                    used.
     * @return the string representation of the supplied header value or {@code null}
     *         if the supplied header value is {@code null}.
     */
    @SuppressWarnings("unchecked")
    private static String asString(final Object headerValue, RuntimeDelegate rd) {
        if (headerValue == null) {
            return null;
        }
        if (headerValue instanceof String) {
            return (String) headerValue;
        }
        if (rd == null) {
            rd = RuntimeDelegate.getInstance();
        }

        final HeaderDelegate hp = rd.createHeaderDelegate(headerValue.getClass());
        return (hp != null) ? hp.toString(headerValue) : headerValue.toString();
    }

    /**
     * Convert a message header value, represented as a general object, to it's
     * string representation. If the supplied header value is {@code null},
     * this method returns {@code null}.
     * <p>
     * This method defers to {@link RuntimeDelegate#createHeaderDelegate} to
     * obtain a {@link HeaderDelegate} or {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     * to convert the value to a {@code String}.
     * If neither is found then the {@code toString()}
     * method on the header object is utilized.
     *
     * @param headerValue   the header value represented as an object.
     * @param configuration the {@link Configuration} that may contain
     *                      {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}
     *                      property preventing the {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     *                      to be used by the default {@code RuntimeDelegate} {@link RuntimeDelegate#getInstance() instance}.
     * @return the string representation of the supplied header value or {@code null}
     *         if the supplied header value is {@code null}.
     */
    public static String asString(final Object headerValue, Configuration configuration) {
        return asString(headerValue, RuntimeDelegateDecorator.configured(configuration));
    }

    /**
     * Returns string view of list of header values. Any modifications to the underlying list are visible to the view,
     * the view also supports removal of elements. Does not support other modifications.
     *
     * @param headerValues header values.
     * @param rd           RuntimeDelegate instance or {@code null} (in that case {@link RuntimeDelegate#getInstance()}
     *                     will be called for before element conversion.
     * @return String view of header values.
     */
    private static List<String> asStringList(final List<Object> headerValues, final RuntimeDelegate rd) {
        if (headerValues == null || headerValues.isEmpty()) {
            return Collections.emptyList();
        }

        return Views.listView(headerValues, input -> (input == null)
                ? "[null]"
                : HeaderUtils.asString(input, rd));
    }

    /**
     * Returns string view of list of header values. Any modifications to the underlying list are visible to the view,
     * the view also supports removal of elements. Does not support other modifications.
     *
     * @param headerValues header values.
     * @param configuration the {@link Configuration} that may contain
     *                      {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}
     *                      property preventing the {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     *                      to be used by the default {@code RuntimeDelegate} {@link RuntimeDelegate#getInstance() instance}.
     * @return String view of header values.
     */
    public static List<String> asStringList(final List<Object> headerValues, final Configuration configuration) {
        return asStringList(headerValues, RuntimeDelegateDecorator.configured(configuration));
    }

    /**
     * Returns string view of passed headers. Any modifications to the headers are visible to the view, the view also
     * supports removal of elements. Does not support other modifications.
     *
     * @param headers headers.
     * @param configuration the {@link Configuration} that may contain
     *                      {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}
     *                      property preventing the {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     *                      to be used by the default {@code RuntimeDelegate} {@link RuntimeDelegate#getInstance() instance}.
     * @return String view of headers or {@code null} if {code headers} input parameter is {@code null}.
     */
    public static MultivaluedMap<String, String> asStringHeaders(
            final MultivaluedMap<String, Object> headers, Configuration configuration) {
        if (headers == null) {
            return null;
        }

        final RuntimeDelegate rd = RuntimeDelegateDecorator.configured(configuration);
        return new AbstractMultivaluedMap<String, String>(
                Views.mapView(headers, input -> HeaderUtils.asStringList(input, rd))
        ) {
        };
    }

    /**
     * Transforms multi value map of headers to single {@code String} value map.
     *
     * Returned map is immutable. Map values are formatted using method {@link #asHeaderString(List, RuntimeDelegate)}.
     *
     * @param headers headers to be formatted
     * @param configuration the {@link Configuration} that may contain
     *                      {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}
     *                      property preventing the {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     *                      to be used by the default {@code RuntimeDelegate} {@link RuntimeDelegate#getInstance() instance}.
     * @return immutable single {@code String} value map or
     *      {@code null} if {@code headers} input parameter is {@code null}.
     */
    public static Map<String, String> asStringHeadersSingleValue(
            final MultivaluedMap<String, Object> headers, Configuration configuration) {
        if (headers == null) {
            return null;
        }

        final RuntimeDelegate rd = RuntimeDelegateDecorator.configured(configuration);
        return Collections.unmodifiableMap(headers.entrySet().stream()
                                           .collect(Collectors.toMap(
                                                   Map.Entry::getKey,
                                                   entry -> asHeaderString(entry.getValue(), rd))));
    }

    /**
     * Converts a list of message header values to a single string value (with individual values separated by
     * {@code ','}).
     *
     * Each single header value is converted to String using a
     * {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if one is available
     * via {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
     * for the header value class or using its {@code toString()} method if a header
     * delegate is not available.
     *
     * @param values list of individual header values.
     * @param rd     {@link RuntimeDelegate} instance or {@code null} (in that case {@link RuntimeDelegate#getInstance()}
     *               will be called for before conversion of elements).
     * @return single string consisting of all the values passed in as a parameter. If values parameter is {@code null},
     *         {@code null} is returned. If the list of values is empty, an empty string is returned.
     */
    public static String asHeaderString(final List<Object> values, final RuntimeDelegate rd) {
        if (values == null) {
            return null;
        }
        final Iterator<String> stringValues = asStringList(values, rd).iterator();
        if (!stringValues.hasNext()) {
            return "";
        }

        final StringBuilder buffer = new StringBuilder(stringValues.next());
        while (stringValues.hasNext()) {
            buffer.append(',').append(stringValues.next());
        }

        return buffer.toString();
    }

    /**
     * Compares two snapshots of headers from jersey {@code ClientRequest} and logs {@code WARNING} in case of difference.
     *
     * Current container implementations does not support header modification in {@link javax.ws.rs.ext.WriterInterceptor}
     * and {@link javax.ws.rs.ext.MessageBodyWriter}. The method checks there are some newly added headers
     * (probably by WI or MBW) and logs {@code WARNING} message about it.
     *
     * @param headersSnapshot first immutable snapshot of headers
     * @param currentHeaders  current instance of headers tobe compared to
     * @param connectorName   name of connector the method is invoked from, used just in logged message
     * @param configuration the {@link Configuration} that may contain
     *                      {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}
     *                      property preventing the {@link org.glassfish.jersey.spi.HeaderDelegateProvider}
     *                      to be used by the default {@code RuntimeDelegate} {@link RuntimeDelegate#getInstance() instance}.
     * @see <a href="https://java.net/jira/browse/JERSEY-2341">JERSEY-2341</a>
     */
    public static void checkHeaderChanges(final Map<String, String> headersSnapshot,
                                          final MultivaluedMap<String, Object> currentHeaders,
                                          final String connectorName,
                                          final Configuration configuration) {
        if (HeaderUtils.LOGGER.isLoggable(Level.WARNING)) {
            final RuntimeDelegate rd = RuntimeDelegateDecorator.configured(configuration);
            final Set<String> changedHeaderNames = new HashSet<String>();
            for (final Map.Entry<? extends String, ? extends List<Object>> entry : currentHeaders.entrySet()) {
                if (!headersSnapshot.containsKey(entry.getKey())) {
                    changedHeaderNames.add(entry.getKey());
                } else {
                    final String prevValue = headersSnapshot.get(entry.getKey());
                    final String newValue = asHeaderString(currentHeaders.get(entry.getKey()), rd);
                    if (!prevValue.equals(newValue)) {
                        changedHeaderNames.add(entry.getKey());
                    }
                }
            }
            if (!changedHeaderNames.isEmpty()) {
                if (HeaderUtils.LOGGER.isLoggable(Level.WARNING)) {
                    HeaderUtils.LOGGER.warning(LocalizationMessages.SOME_HEADERS_NOT_SENT(connectorName,
                            changedHeaderNames.toString()));
                }
            }
        }
    }

    /**
     * Convert a message header value, represented as a general object, to it's
     * string representation. If the supplied header value is {@code null},
     * this method returns {@code null}.
     *
     * @param headerValue   the header value represented as an object.
     * @return the string representation of the supplied header value or {@code null}
     *         if the supplied header value is {@code null}.
     * @see #asString(Object, Configuration)
     */
    @Deprecated
    public static String asString(final Object headerValue) {
        return asString(headerValue, (Configuration) null);
    }

    /**
     * Returns string view of list of header values. Any modifications to the underlying list are visible to the view,
     * the view also supports removal of elements. Does not support other modifications.
     *
     * @param headerValues header values.
     * @return String view of header values.
     * @see #asStringList(List, Configuration)
     */
    @Deprecated
    public static List<String> asStringList(final List<Object> headerValues) {
        return asStringList(headerValues, (Configuration) null);
    }

    /**
     * Returns string view of passed headers. Any modifications to the headers are visible to the view, the view also
     * supports removal of elements. Does not support other modifications.
     *
     * @param headers headers.
     * @return String view of headers or {@code null} if {code headers} input parameter is {@code null}.
     * @see #asStringHeaders(MultivaluedMap, Configuration)
     */
    @Deprecated
    public static MultivaluedMap<String, String> asStringHeaders(final MultivaluedMap<String, Object> headers) {
        return asStringHeaders(headers, (Configuration) null);
    }

    /**
     * Transforms multi value map of headers to single {@code String} value map.
     *
     * Returned map is immutable. Map values are formatted using method {@link #asHeaderString(List, RuntimeDelegate)}.
     *
     * @param headers headers to be formatted
     * @return immutable single {@code String} value map or
     *      {@code null} if {@code headers} input parameter is {@code null}.
     * @see #asStringHeadersSingleValue(MultivaluedMap, Configuration)
     */
    @Deprecated
    public static Map<String, String> asStringHeadersSingleValue(final MultivaluedMap<String, Object> headers) {
        return asStringHeadersSingleValue(headers, (Configuration) null);
    }

    /**
     * Compares two snapshots of headers from jersey {@code ClientRequest} and logs {@code WARNING} in case of difference.
     *
     * Current container implementations does not support header modification in {@link javax.ws.rs.ext.WriterInterceptor}
     * and {@link javax.ws.rs.ext.MessageBodyWriter}. The method checks there are some newly added headers
     * (probably by WI or MBW) and logs {@code WARNING} message about it.
     *
     * @param headersSnapshot first immutable snapshot of headers
     * @param currentHeaders  current instance of headers tobe compared to
     * @param connectorName   name of connector the method is invoked from, used just in logged message
     * @see #checkHeaderChanges(Map, MultivaluedMap, String, Configuration)
     */
    @Deprecated
    public static void checkHeaderChanges(final Map<String, String> headersSnapshot,
                                          final MultivaluedMap<String, Object> currentHeaders,
                                          final String connectorName) {
        checkHeaderChanges(headersSnapshot, currentHeaders, connectorName, (Configuration) null);
    }

    /**
     * Preventing instantiation.
     */
    private HeaderUtils() {
        throw new AssertionError("No instances allowed.");
    }
}
